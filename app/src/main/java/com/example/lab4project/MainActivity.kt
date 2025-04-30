package com.example.lab4project

import android.app.Application
import android.content.Context
import android.icu.lang.UCharacter.VerticalOrientation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.lab4project.ui.theme.Lab4ProjectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab4ProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)){
                        ShoppingListScreen()
                    }
                }
            }
        }
    }
}

// Model
@Entity(tableName="shopping_items")
data class ShoppingItem(
    val name: String,
    val isBought: Boolean = false,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

// ORM
@Dao
interface ShoppingDao{
    @Query("SELECT * FROM shopping_items ORDER BY id DESC")
    fun getAllItems(): List<ShoppingItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: ShoppingItem)

    @Update
    fun updateItem(item: ShoppingItem)

    @Delete
    fun deleteItem(item: ShoppingItem)
}

@Database(entities = [ShoppingItem::class], version = 1)
abstract class ShoppingDataBase: RoomDatabase(){
    abstract fun shoppingDao(): ShoppingDao

    companion object{
        @Volatile
        private var INSTANCE : ShoppingDataBase? = null

        fun getInstance(context: Context): ShoppingDataBase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShoppingDataBase::class.java,
                    "shopping_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ViewModel
class ShoppingListViewModel(application: Application): AndroidViewModel(application){

    private val dao: ShoppingDao = ShoppingDataBase.getInstance(application).shoppingDao()
    private val _shoppingList = mutableStateListOf<ShoppingItem>()
    val shoppingList: List<ShoppingItem> get() = _shoppingList

    init{
        loadShoppingList()
    }

    private fun loadShoppingList(){
        viewModelScope.launch (Dispatchers.IO) {
            val items = dao.getAllItems()
            _shoppingList.clear()
            _shoppingList.addAll(items)
        }
    }

    fun addItem(name: String){
        viewModelScope.launch(Dispatchers.IO){
            val newItem = ShoppingItem(name = name)
            dao.insertItem(newItem)
            loadShoppingList()
        }
    }

    // Додав функцію видалення
    fun deleteItem(index: Int){
        viewModelScope.launch(Dispatchers.IO){
            val item = _shoppingList[index]
            dao.deleteItem(item)
            loadShoppingList()
        }
    }

    fun toggleBought(index: Int){
        viewModelScope.launch(Dispatchers.IO){
            val item = _shoppingList[index]
            val updateItem = item.copy(isBought = !item.isBought)
            dao.updateItem(updateItem)
            _shoppingList[index] = updateItem
        }
    }
}

// View
@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    onToggleBought: () -> Unit = {},
    // Додав обробник події видалення
    onDelete: () -> Unit = {}
){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                Color.LightGray,
                // MaterialTheme.colorScheme.surfaceDim,
                MaterialTheme.shapes.large
            )
            //.clickable {onToggleBought()}
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Checkbox(checked = item.isBought, onCheckedChange = {
            onToggleBought()
        })
        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
        )
        // Додав кнопку видалення в UI
        Button(
            modifier = Modifier.height(60.dp).width(90.dp),
            onClick = onDelete
        ) {
            Text("Delete")
        }
    }
}

class ShoppingListViewModelFactory(private val application: Application):
    ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AddItemButton(addItem: (String) -> Unit = {}){
    var text by remember { mutableStateOf("") }

    Row(modifier = Modifier
        .padding(top = 25.dp)
        .fillMaxWidth()
        .height(80.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.align(Alignment.CenterVertically),
            value = text,
            onValueChange = { text = it},
            label = { Text("Add Item")}
        )
        Button(
            modifier = Modifier
                .padding(start = 5.dp, top = 5.dp)
                .height(60.dp)
                .width(80.dp)
                .align(Alignment.CenterVertically),
            onClick = {
                if (text.isNotEmpty()){
                    addItem(text)
                    text = ""
                }
            }) {
            Text("Add")
        }
    }
}

@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel = viewModel(
    factory = ShoppingListViewModelFactory(LocalContext.current.applicationContext as Application)
)){
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ) {
        item{
            AddItemButton{viewModel.addItem(it)}
        }
        // Змінив принцип призначення лямба-функцій, вказавши всі ім'я параметрів
        itemsIndexed(viewModel.shoppingList){ ix, item ->
            ShoppingItemCard(
                item = item,
                onToggleBought = { viewModel.toggleBought(ix) },
                onDelete = { viewModel.deleteItem(ix) }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShoppingListScreenPreview(){
    val testList = listOf(
        ShoppingItem("Молоко", true),
        ShoppingItem("Хліб"),
        ShoppingItem("Яйця")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ) {
        item{
            AddItemButton{}
        }
        itemsIndexed(testList){ ix, item ->
            ShoppingItemCard(item)
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun ShoppingItemCardPreview(){
    var toggleState by remember {mutableStateOf(false)}
    ShoppingItemCard(ShoppingItem("Молоко", isBought = toggleState)
    ) {
        toggleState = !toggleState
    }
}
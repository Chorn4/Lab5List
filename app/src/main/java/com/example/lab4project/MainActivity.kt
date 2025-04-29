package com.example.lab4project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab4project.ui.theme.Lab4ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab4ProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)){

                    }
                }
            }
        }
    }
}

// Model
class ShoppingItem(
    val name: String,
    var isBought: Boolean = false
)

// View
@Composable
fun ShoppingItemCard(item: ShoppingItem){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceDim,
                MaterialTheme.shapes.large
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Checkbox(checked = item.isBought, onCheckedChange = { })
        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
        )

    }
}

@Preview(showBackground = true)
@Composable
fun ShoppingItemCardPreview(){
    ShoppingItemCard(ShoppingItem("М олоко"))

}
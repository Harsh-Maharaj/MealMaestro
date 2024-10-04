import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Ingredient  // Make sure to import the Ingredient class if it's in a different file
import com.example.mealmaestro.R

class ShoppingListAdapter(private val ingredients: MutableList<Ingredient>) :
    RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.text_ingredient_name)
        val boughtCheckBox: CheckBox = view.findViewById(R.id.checkbox_bought)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.nameTextView.text = ingredient.name
        holder.boughtCheckBox.isChecked = ingredient.isBought
        holder.boughtCheckBox.setOnCheckedChangeListener { _, isChecked ->
            ingredient.isBought = isChecked
        }
    }

    override fun getItemCount() = ingredients.size
}

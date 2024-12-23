import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.lifecalendar.R

class ButtonAdapter(
    private var items: MutableList<String>, 
    private var weeksDiff: Int = 0,
    private var showNumbers: Boolean
) : RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>() {

    private var weeks: Int = 0

    class ButtonViewHolder(val button: Button) : RecyclerView.ViewHolder(button)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val button = Button(parent.context).apply {
            background = parent.context.getDrawable(R.drawable.rounded_button)

            val params = ViewGroup.MarginLayoutParams(
                140,
                140
            ).apply {
                setMargins(10, 10, 10, 10)
            }
            layoutParams = params
        }

        return ButtonViewHolder(button)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val params = holder.button.layoutParams as ViewGroup.MarginLayoutParams
        if (position % 48 in 0..5) {
            params.setMargins(10, 150, 10, 10)
        } else {
            params.setMargins(10, 10, 10, 10)
        }
        
        holder.button.text = if (showNumbers) items[position] else ""
        
        when {
            position == weeksDiff -> {
                holder.button.alpha = 1.0f
                holder.button.background = holder.button.context.getDrawable(R.drawable.rounded_button_now)
            }
            position < weeksDiff -> {
                holder.button.alpha = 0.5f
                holder.button.background = holder.button.context.getDrawable(R.drawable.rounded_button_gray)
            }
            else -> {
                holder.button.alpha = 1.0f
                holder.button.background = holder.button.context.getDrawable(R.drawable.rounded_button)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: MutableList<String>, newWeeksDiff: Int, showNumbers: Boolean = true) {
        items = newItems
        weeksDiff = newWeeksDiff
        this.showNumbers = showNumbers
        notifyDataSetChanged()
        Log.d("updateItemDebug", "updateItems: weeksDiff=$weeksDiff")
    }
}
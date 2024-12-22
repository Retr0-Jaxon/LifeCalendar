import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifecalendar.R

class MemoAdapter(
    private var memos: MutableList<Memo>,
    private val onMemoChecked: (Int) -> Unit
) : RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())

    data class Memo(
        val id: Int,
        val content: String,
        val timestamp: Long
    )

    class MemoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.memo_content)
        val checkbox: CheckBox = itemView.findViewById(R.id.memo_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memo, parent, false)
        view.alpha = 1.0f
        return MemoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = memos[position]
        holder.content.text = memo.content
        holder.itemView.alpha = 1.0f
        holder.checkbox.isChecked = false
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                handler.postDelayed({
                    holder.itemView.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                onMemoChecked(memo.id)
                            }
                        })
                }, 600)
            }
        }
    }

    override fun getItemCount() = memos.size

    fun updateMemos(newMemos: List<Memo>) {
        memos.clear()
        memos.addAll(newMemos)
        notifyDataSetChanged()
    }
}

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.sanggoroe.R
import com.capstone.sanggoroe.model.Post

class RecommendationAdapter(
    private val context: Context
) : ListAdapter<Post, RecommendationAdapter.RecommendationViewHolder>(DIFF_CALLBACK) {

    inner class RecommendationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postImage: ImageView = view.findViewById(R.id.postImageView)
        val postTitle: TextView = view.findViewById(R.id.titleContent)
        val postContent: TextView = view.findViewById(R.id.postContentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemlist_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val post = getItem(position)
        Glide.with(context).load(post.image).into(holder.postImage)
        holder.postTitle.text = post.title
        holder.postContent.text = post.content
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem.jobID == newItem.jobID
            }

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }
        }
    }
}

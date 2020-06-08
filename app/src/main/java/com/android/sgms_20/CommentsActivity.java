package com.android.sgms_20;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    private ImageButton postCommentButton;
   // ImageView end;

    private EditText CommentInputText;
    private RecyclerView CommentsList;
    private String Post_Key,current_user_id;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        getWindow().setLayout((int)(width*.90),(int) (height*.80));

        WindowManager.LayoutParams windowManager = getWindow().getAttributes();
        windowManager.dimAmount = 0.60f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        mAuth= FirebaseAuth.getInstance();
        current_user_id=mAuth.getCurrentUser().getUid();
        //end=findViewById(R.id.end);
        Post_Key=getIntent().getExtras().get("PostKey").toString();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        CommentsList=(RecyclerView)findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText =(EditText)findViewById(R.id.comment_input);
        postCommentButton=(ImageButton)findViewById(R.id.post_comment_button);




        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(current_user_id.equals("FU5r1KMEvOeQqCU5D8V7FQ4MGQW2"))
                {
                    startActivity(new Intent(CommentsActivity.this,SnackBarActivity.class));
                    finish();
                }
                else {
                    UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                String userName=dataSnapshot.child("username").getValue().toString();
                                ValidateComment(userName);
                                CommentInputText.setText("");
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }


            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Comments> options=
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(PostsRef,Comments.class)
                        .setLifecycleOwner(this)
                        .build();
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new CommentsActivity.CommentsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_comments_layout, parent, false));

            }

            @Override
            protected void onBindViewHolder(@NonNull CommentsActivity.CommentsViewHolder commentsViewHolder, int i, @NonNull Comments comments)
            {
                commentsViewHolder.setUsername(comments.getUsername());
                commentsViewHolder.setComment(comments.getComment());
                commentsViewHolder.setDate(comments.getDate());
                commentsViewHolder.setTime(comments.getTime());


            }
        };

        CommentsList.setAdapter(firebaseRecyclerAdapter);



    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public CommentsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView=itemView;
        }

        public void setUsername(String username)
        {
            TextView myUserName=(TextView)mView.findViewById(R.id.comment_username);
            myUserName.setText( " " +username+" ");
        }
        public void setComment(String comment)
        {
            TextView myComment=(TextView)mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }
        public void setTime(String time)
        {
            TextView myTime=(TextView)mView.findViewById(R.id.comment_time);
            myTime.setText(" Time: "+time);
        }
        public void setDate(String date)
        {
            TextView myDate=(TextView)mView.findViewById(R.id.comment_date);
            myDate.setText(" Date: "+date);
        }



    }

    private void ValidateComment(String userName)
    {
        String commentText=CommentInputText.getText().toString();
        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please enter your comment..", Toast.LENGTH_SHORT).show();
        }
        else
        {

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String  saveCurrentTime = currentTime.format(calFordDate.getTime());

            final String RandomKey= saveCurrentDate + saveCurrentTime+current_user_id ;

            HashMap commentsMap=new HashMap();
            commentsMap.put("uid",current_user_id);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",userName);
            PostsRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(CommentsActivity.this, "Your comment is added successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(CommentsActivity.this, "Error occured..Try again", Toast.LENGTH_SHORT).show();
                }
                }
            });

        }
    }
}

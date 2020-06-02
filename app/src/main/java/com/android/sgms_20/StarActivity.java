package com.android.sgms_20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.yalantis.filter.adapter.FilterAdapter;
import com.yalantis.filter.animator.FiltersListItemAnimator;
import com.yalantis.filter.listener.FilterListener;
import com.yalantis.filter.widget.Filter;
import com.yalantis.filter.widget.FilterItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import greco.lorenzo.com.lgsnackbar.LGSnackbarManager;
import greco.lorenzo.com.lgsnackbar.core.LGSnackbarAction;

import static greco.lorenzo.com.lgsnackbar.style.LGSnackBarTheme.SnackbarStyle.SUCCESS;
import static greco.lorenzo.com.lgsnackbar.style.LGSnackBarTheme.SnackbarStyle.WARNING;

public class StarActivity extends AppCompatActivity implements FilterListener<Tag> {
    private FirebaseAuth mAuth;

    private GoogleApiClient mGoogleApiClient;
    String currentUserID;
    private int[] mColors;
    private String[] mAdmin = new String[]{"AkX6MclvgrXpN8oOGI5v37dn7eb2"};
    private String[] mClub;
    private int colour1, colour2, colour3, colour4;
    private DatabaseReference UsersRef;
    private Button mSort1, mSort2;
    private Toolbar mToolbar;
    private TextDrawable mDrawableBuilder;
    private String[] mTitles;
    private List<Posts> mAllQuestions;
    private Filter<Tag> mFilter;
    private LinearLayoutManager linearLayoutManager;
    private PostsAdapter mAdapter;
    AppCompatImageView LikePostButton, downVotePostButton;
    TextView DisplayNoOfLikes, DisplayDownVotes;
    int CountLikes, countDownVotes;

    int pos;
    private ImageView pro;
    //private int q;

    private RecyclerView postList, mRecyclerView;

    private DatabaseReference MyPostRef, PostsRef, LikesRef, DownVotesRef;
    String letter = "A";


    boolean LikeChecker = false, DownVoteChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star);
        //q=0;
        mToolbar = (Toolbar) findViewById(R.id.toolbar1);
        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications");//subscribing
        FirebaseMessaging.getInstance().unsubscribeFromTopic("pushNotifications");//unsubscribe

        pro = (ImageView) findViewById(R.id.thumbnail1);
        setSupportActionBar(mToolbar);
        setTitle("Home");


        if (!haveNetworkConnection()) {
            Toast.makeText(StarActivity.this, "You are not Online....Please switch on your interner connection!", Toast.LENGTH_LONG).show();
        }

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DownVotesRef = FirebaseDatabase.getInstance().getReference().child("DownVotes");
        MyPostRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        //pro.setVisibility(View.INVISIBLE);
        MyPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("username")) {
                        String myProfileName = dataSnapshot.child("username").getValue().toString();
                        char letter = myProfileName.charAt(0);
                        letter = Character.toUpperCase(letter);


                        mDrawableBuilder = TextDrawable.builder().buildRound(String.valueOf(letter), R.color.colorAccent);

                        pro.setImageDrawable(mDrawableBuilder);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StarActivity.this, SideMenu.class));

            }
        });
        ImagePipelineConfig config = ImagePipelineConfig
                .newBuilder(this)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);

        mColors = getResources().getIntArray(R.array.colors);
        mAdmin = getResources().getStringArray(R.array.admin_uid);
        mTitles = getResources().getStringArray(R.array.job_titles);
        mClub = getResources().getStringArray(R.array.club_uid);

        mFilter = (Filter<Tag>) findViewById(R.id.filter1);
        mFilter.setAdapter(new Adapter(getTags()));
        mFilter.setListener(this);

        //the text to show when there's no selected items
        mFilter.setNoSelectedItemText(getString(R.string.str_all_selected));
        mFilter.build();


        mRecyclerView = (RecyclerView) findViewById(R.id.list1);
        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        pos = linearLayoutManager.findLastVisibleItemPosition();
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new PostsAdapter(this, mAllQuestions = getQuestions());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setAdapter(mAdapter = new PostsAdapter(this, mAllQuestions = getQuestions()));

        mRecyclerView.setItemAnimator(new FiltersListItemAnimator());
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation1);

        if (currentUserID.equals("FU5r1KMEvOeQqCU5D8V7FQ4MGQW2")) {
            bottomNav.setOnNavigationItemSelectedListener(navListner1);
        } else {
            bottomNav.setOnNavigationItemSelectedListener(navListner);
        }
        bottomNav.getMenu().findItem(R.id.nav_star).setChecked(true);
    }


    private void calculateDiff(final List<Posts> oldList, final List<Posts> newList) {
        DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }
        }).dispatchUpdatesTo(mAdapter);
    }

    private List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();

        for (int i = 0; i < mTitles.length; ++i) {
            tags.add(new Tag(mTitles[i], mColors[i]));
        }

        return tags;
    }

    @Override
    public void onNothingSelected() {
        if (mRecyclerView != null) {
            mAdapter.setPosts(mAllQuestions);
            mAdapter.notifyDataSetChanged();
        }
    }


    private List<Posts> getQuestions() {
        return new ArrayList<Posts>() {
            {


                MyPostRef.child("star").addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mAllQuestions.clear();

                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            String postKey = dataSnapshot1.child("PostKey").getValue().toString();
                            //final String PostKey=dataSnapshot1.getKey();
                            /*Intent intent=new Intent(MainActivity.this,PostsAdapter.class);
                            intent.putExtra("PostKey",PostKey);
                            startActivity(intent);*/
                            final String owner;
                            String uid = dataSnapshot1.child("uid").getValue().toString();
                            int c = 0;
                            for (int i = 0; i < 1; i++) {
                                if (uid.equals(mAdmin[i])) {
                                    c = 1;
                                    break;
                                }
                            }
                            if (c != 1) {
                                for (int j = 0; j < 1; j++) {
                                    if (uid.equals(mClub[j])) {
                                        c = 2;
                                        break;
                                    }
                                }
                            }

                            if (c == 1) {
                                owner = "Admin";
                                c = 0;
                            } else if (c == 2) {
                                owner = "Club";
                                c = 0;
                            } else if (currentUserID.equals(uid)) {
                                owner = "MyPosts";
                                c = 0;
                            } else {
                                owner = "General";
                                c = 0;
                            }


                            String show = dataSnapshot1.child("showInformation").getValue().toString();
                            String info, mail;
                            //if(show.equals("no"))info="Anonymous";


                            final String mode = dataSnapshot1.child("mode").getValue().toString();
                            final String sub = dataSnapshot1.child("subCategory").getValue().toString();
                            final String categ = dataSnapshot1.child("category").getValue().toString();
                            String name = dataSnapshot1.child("username").getValue().toString();
                            String status;
                            if (!owner.equals("Admin"))
                                status = dataSnapshot1.child("status").getValue().toString();
                            else status = "-";
                            String postpic = dataSnapshot1.child("PostImage").getValue().toString();
                            String user = dataSnapshot1.child("email").getValue().toString();
                            String date = dataSnapshot1.child("date").getValue().toString();
                            String post = dataSnapshot1.child("description").getValue().toString();
                            //    String profilePic = dataSnapshot1.child("profileImage").getValue().toString();
                            if (show.equals("no")) {
                                info = "Anonymous";
                                mail = " ";
                            } else {
                                info = name;
                                mail = user;
                            }

                            if (categ.equals("Official")) colour1 = mColors[1];
                            if (categ.equals("Personal")) colour1 = mColors[2];
                            if (categ.equals("Miscellaneous")) colour1 = mColors[3];
                            if (sub.equals("Admission")) colour2 = mColors[4];
                            if (sub.equals("Academic")) colour2 = mColors[5];
                            if (sub.equals("Finance")) colour2 = mColors[6];
                            if (sub.equals("Housing")) colour2 = mColors[7];
                            if (sub.equals("Rights Violation")) colour2 = mColors[8];
                            if (sub.equals("Health")) colour2 = mColors[9];
                            if (sub.equals("Placements")) colour2 = mColors[19];
                            if (mode.equals("Public")) colour3 = mColors[10];
                            if (sub.equals("Internships")) colour2 = mColors[11];
                            if (sub.equals("Competitions")) colour2 = mColors[12];
                            if (sub.equals("Courses")) colour2 = mColors[13];
                            if (mode.equals("Private")) colour3 = mColors[14];
                            if (owner.equals("Admin")) colour4 = mColors[15];
                            if (owner.equals("General")) colour4 = mColors[16];
                            if (owner.equals("MyPosts")) colour4 = mColors[17];
                            if (owner.equals("Club")) colour4 = mColors[18];


                            if (mode.equals("Public")) {
                                add(new Posts(postKey, "" + info, mail, post, date, date, uid, mode, postpic, categ, sub, show, status, new ArrayList<Tag>() {{
                                    add(new Tag(owner, colour4));
                                    add(new Tag(mode, colour3));
                                    add(new Tag(categ, colour1));
                                    add(new Tag(sub, colour2));


                                }}));
                            } else {
                                int l = 0;
                                for (int i = 0; i < 1; i++) {
                                    if (currentUserID.equals(mAdmin[i])) {
                                        l = 1;
                                        break;
                                    }
                                }
                                if (l == 1 || (uid.equals(currentUserID))) {
                                    l = 0;
                                    add(new Posts(postKey, info, "" + user, post, date, date, uid, mode, postpic, categ, sub, show, status, new ArrayList<Tag>() {{
                                        add(new Tag(owner, colour4));
                                        add(new Tag(mode, colour3));
                                        add(new Tag(categ, colour1));
                                        add(new Tag(sub, colour2));
                                    }}));
                                }
                            }
                        }
                        mAdapter = new PostsAdapter(StarActivity.this, mAllQuestions);
                        mRecyclerView.setAdapter(mAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

    };
}

    private List<Posts> findByTags(List<Tag> tags) {
        List<Posts> questions = new ArrayList<>();

        for (Posts question : mAllQuestions) {
            for (Tag tag : tags) {
                if (question.hasTag(tag.getText()) && !questions.contains(question)) {
                    questions.add(question);
                }
            }
        }

        return questions;
    }

    @Override
    public void onFiltersSelected(@NotNull ArrayList<Tag> filters) {
        List<Posts> newQuestions = findByTags(filters);
        List<Posts> oldQuestions = mAdapter.getPosts();
        mAdapter.setPosts(newQuestions);
        calculateDiff(oldQuestions, newQuestions);
        mAdapter.notifyDataSetChanged();

        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onFilterSelected(Tag item) {
        if (item.getText().equals(mTitles[0])) {
            mFilter.deselectAll();
            mFilter.collapse();
        }
    }

    @Override
    public void onFilterDeselected(Tag item) {

    }

    class Adapter extends FilterAdapter<Tag> {

        Adapter(@NotNull List<? extends Tag> items) {
            super(items);
        }

        @NotNull
        @Override
        public FilterItem createView(int position, Tag item) {
            FilterItem filterItem = new FilterItem(StarActivity.this);

            filterItem.setStrokeColor(R.color.colorPrimary);
            filterItem.setTextColor(R.color.colorPrimary);
            //filterItem.setCornerRadius(14);
            filterItem.setCheckedTextColor(ContextCompat.getColor(StarActivity.this, android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(StarActivity.this, android.R.color.white));
            filterItem.setCheckedColor(mColors[position]);
            filterItem.setText(item.getText());
            filterItem.deselect();

            return filterItem;
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListner1=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.nav_post:
                            SendUserToLoginActivity();
                            break;
                        case R.id.nav_profile:
                            SendUserToLoginActivity();
                            break;
                        case R.id.nav_star:
                            SendUserToLoginActivity();
                            break;
                    }
                    return true;
                }
            };

    private void SendUserToLoginActivity()
    {
        Intent loginIntent=new Intent(StarActivity.this,SnackBarActivity.class);
        startActivity(loginIntent);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListner=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.nav_home:
                            startActivity(new Intent(StarActivity.this,MainActivity.class));
                            break;

                        case R.id.nav_post:
                            Intent intent=new Intent(StarActivity.this,PostActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_profile:
                            Intent Pintent=new Intent(StarActivity.this,ProfileActivity.class);
                            startActivity(Pintent);
                            break;
                        case R.id.nav_star:
                            break;
                    }

                    return true;
                }
            };

    @Override
    protected void onStart() {


        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        if(currentUser==null)
        {
            //sendUserToLoginActivity();
           /* mAuth=FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword("withoutloginuser@gmail.com","LoginFast").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
            });*/
        }
        else
        {
            CheckUserExistence();
        }

    }

    private void CheckUserExistence()
    {
        final String current_user_id=mAuth.getCurrentUser().getUid();//online user
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id))
                {
                    SendToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendToSetupActivity()
    {
        Intent intent=new Intent(StarActivity.this, SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent=new Intent(StarActivity.this,LoginActivity.class);

        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

}


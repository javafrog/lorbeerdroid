package de.lorbeer.app;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.lorbeer.handler.CommentHandler;
import de.lorbeer.helper.ConnectionChecker;
import de.lorbeer.rest.RequestMethod;
import de.lorbeer.rest.RestClient;
import de.mmt.lorbeerblatt.data.Comment;

public class CommentActivity extends Activity
{
private static final int SUCCESSFUL_ADDED = 111;
private CommentHandler   commentHandler;
private ListView         comment_list;
private int              d_id;
private TextView         noComments;

@Override
public void onCreate(Bundle savedInstanceState)
  {
   super.onCreate(savedInstanceState);
   setContentView(R.layout.comments);
   
   // set name of dish
   TextView tv = (TextView)findViewById(R.id.dishName);
   tv.setText(getIntent().getExtras().getString("d_name"));
   
   comment_list = new ListView(getApplicationContext());

   // load data from REST
   if (ConnectionChecker.isNetworkReachable(getApplicationContext(),true))
     {
      getData();
     }
   else
     {
      // no connection available
      Toast.makeText(getApplicationContext(),R.string.noConnection,Toast.LENGTH_SHORT).show();
     }
  }

private void getData()
  {
   // get data from RESTservice
   d_id = getIntent().getExtras().getInt("d_id");
   RestClient rc = new RestClient(MensaActivity.basicURI + "dishes" + "/" + d_id + "/comments");
   try
     {
      rc.Execute(RequestMethod.GET);
     }
   catch (Exception e)
     {
      Toast.makeText(getApplicationContext(),R.string.error,Toast.LENGTH_SHORT).show();
     }

   String resp = rc.getResponse();
   
   if (!rc.timeout)
     {
      try
        {
         // create parser
         SAXParserFactory spf = SAXParserFactory.newInstance();
         SAXParser sp = spf.newSAXParser();
         XMLReader xr = sp.getXMLReader();

         commentHandler = new CommentHandler();
         xr.setContentHandler(commentHandler);

         InputSource is = new InputSource(new StringReader(resp));
         is.setEncoding("UTF-8");
         xr.parse(is);

         // fill the list with comments
         fillList();
        }
      catch (Exception e)
        {
         Toast.makeText(getApplicationContext(),R.string.error,Toast.LENGTH_SHORT).show();
        }
     }
  }

private void fillList()
  {
   // get comments
   List<Comment> temp_comments = commentHandler.getComments();

   if (temp_comments.size() == 0)
     {
      // set noCommentsText
      noComments = new TextView(getApplicationContext());
      noComments.setText(R.string.noCommentsToShow);
      noComments.setGravity(1);
      noComments.setPadding(0,30,0,0);

      LinearLayout commentLayout = (LinearLayout)findViewById(R.id.testLayout777);
      commentLayout.addView(noComments);
     }
   else
     {
      // fill comment list
      List<String> comments = new LinkedList<String>();
      
      for (Comment c:temp_comments)
        {
         String text = c.authorName + ": " + c.text;
         comments.add(text);
        }
      
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,comments);
      
      comment_list.setAdapter(adapter);
      comment_list.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
      // add to layout
      LinearLayout commentLayout = (LinearLayout)findViewById(R.id.testLayout777);
      commentLayout.addView(comment_list);
     }
  }

public void addComment(View v)
  {
   Intent i = new Intent(CommentActivity.this,AddCommentActivity.class);
   i.putExtra("d_id",d_id);
   startActivityForResult(i,SUCCESSFUL_ADDED);
  }

@SuppressWarnings(  {"static-access","unchecked"})
protected void onActivityResult(int requestCode,int resultCode,Intent data)
  {
   if (requestCode != SUCCESSFUL_ADDED) return;
   if (resultCode != RESULT_OK) return;

   // update list
   LinearLayout commentLayout = (LinearLayout)findViewById(R.id.testLayout777);
   commentLayout.removeAllViews();
   getData();
   
   ArrayAdapter<String> adapter = (ArrayAdapter<String>)comment_list.getAdapter();
   adapter.notifyDataSetChanged();
   
   // set no comments text gone
   if (noComments != null) noComments.setVisibility(noComments.GONE);
   
   // make toast
   Toast.makeText(getApplicationContext(),R.string.commentSuccessful,Toast.LENGTH_SHORT).show();
  }
}
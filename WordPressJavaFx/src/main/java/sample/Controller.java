package sample;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class Controller {
   private String username;
   private String password;
   private URL xmlRpcUrl;
   private XmlRpcClient client;
   private int blogId = 161853477;
   private List params;

    public Controller(){
        username = "thakursuryadeo";
        password = "Test123456";
        try {
            xmlRpcUrl = new URL("https://animeshvideo.wordpress.com/xmlrpc.php");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        params = new ArrayList();
        client = setClientConfigForRpc();


    }


    /**-- Info about blog including the blogid can be retrieved here --**/

    public void getUsersBlogsInfo(){
        params.add(username);
        params.add(password);

        try {

            Object[] v = (Object[]) client.execute("wp.getUsersBlogs", params);
            if(v == null) {
                System.out.println("No info about user's blogs can be retrieved");
            }else{
                Object[] objects = v;
                for (Object obj : objects) {
                    System.out.println(obj);
                }
            }
        }catch (XmlRpcException e){
            e.printStackTrace();
        }

    }

    public void getPosts(){

        params.add(blogId);
        params.add(username);
        params.add(password);

        try {

            Object v = client.execute("wp.getPosts", params);
            if(v == null) {
                System.out.println("it is null");
            }else{
                Object[] objects = (Object[]) v;
                for (Object obj : objects) {
                    System.out.println(obj);
                }
            }
        }catch (XmlRpcException e){
            e.printStackTrace();
        }

    }

    public void publishPost(){

        Hashtable<String,Object> post = new Hashtable<String,Object>();
        post.put("post_title", "Another post that has been generated through java");
        post.put("post_content", "Success");
        post.put("post_status", "publish");
        post.put("post_date", Date.class);
        post.put("comment_status", "open");
        post.put("ping_status", "open");
        params.add(blogId);
        params.add(username);
        params.add(password);
        params.add(post);

        try {

            Object v = client.execute("wp.newPost", params);
            if(v == null) {
                System.out.println("Error publishing post");
            }else{
                Object obj = v;

                System.out.println(obj.toString());
            }
        }catch (XmlRpcException e){
            e.printStackTrace();
        }

    }

    public void deletePost(int postId){


    }

    public void editPost(int postId){

    }

    public XmlRpcClient setClientConfigForRpc(){
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setBasicPassword(password);
        config.setBasicUserName(username);
        config.setServerURL(xmlRpcUrl);
        config.setEnabledForExtensions(true);
        config.setEnabledForExceptions(true);
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        return client;
    }



}

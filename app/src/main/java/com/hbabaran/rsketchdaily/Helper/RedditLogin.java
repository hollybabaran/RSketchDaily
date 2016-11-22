package com.hbabaran.rsketchdaily.Helper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.hbabaran.rsketchdaily.Activity.RedditLoginActivity;
import com.hbabaran.rsketchdaily.Activity.SubmissionActivity;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.auth.TokenStore;
import net.dean.jraw.auth.VolatileTokenStore;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthException;

/**
 * Created by wren on 17/11/2016.
 */

public class RedditLogin {
    private RedditClient redditClient;
    private RefreshTokenHandler refreshTokenHandler;
    private AuthenticationManager am;


    public RedditLogin(){
        this.redditClient = new RedditClient(
                UserAgent.of("android",
                             "com.hbabaran.rsketchdaily",
                             "v0.01",
                             "hbabaran"));
        this.refreshTokenHandler = new RefreshTokenHandler(new VolatileTokenStore(), //TODO do we have to implement token store???
                this.redditClient);
        this.am = AuthenticationManager.get();
        this.am.init(this.redditClient, this.refreshTokenHandler);

    }

    public void refreshLogin(Context context){
        AuthenticationState state = this.am.checkAuthState();
        System.out.println("resuming submission activity: "+state);
        switch (state) {
            case READY:
                break;
            case NONE:
                Toast.makeText(context, "Log in first", Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(context, RedditLoginActivity.class));
                break;
            case NEED_REFRESH:
                new refreshAccessTokenAsync().execute();
                break;
        }
    }

    public Boolean isReady(){
        return this.am.checkAuthState() == AuthenticationState.READY;
    }
    public RedditClient client(){ return redditClient; }
    private class refreshAccessTokenAsync extends AsyncTask<Credentials, Void, Void> {
        protected Void doInBackground(Credentials... params) {
            try {
                AuthenticationManager.get().refreshAccessToken(RedditLoginActivity.CREDENTIALS);
            } catch (NoSuchTokenException | OAuthException e) {
                System.err.println("Could not refresh access token: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            System.out.println("Successfully refreshed login token");
        }
    }
}


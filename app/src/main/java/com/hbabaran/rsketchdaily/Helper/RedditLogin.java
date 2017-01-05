package com.hbabaran.rsketchdaily.Helper;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.hbabaran.rsketchdaily.Activity.RedditLoginActivity;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.auth.TokenStore;
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

    public class AndroidTokenStore implements TokenStore{
        SharedPreferences prefs;
        private AndroidTokenStore(SharedPreferences prefs){
            this.prefs = prefs;
        }

        /** Checks if a token is already stored */
        public boolean isStored(String key){
            return prefs.getString(key, null) != null;
        }

        /**
         * Gets a token. If none is found, then a {@link NoSuchTokenException} is thrown
         * @throws NoSuchTokenException If the given key does not have a value
         */
        public String readToken(String key) throws NoSuchTokenException{
            String token = prefs.getString(key, null);
            if(token == null) throw new NoSuchTokenException();
            return token;
        }

        /** Writes a token. */
        public void writeToken(String key, String token){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(key, token);
            editor.commit();
        }
    }


    public RedditLogin(SharedPreferences prefs){
        this.redditClient = new RedditClient(
                UserAgent.of("android",
                             "com.hbabaran.rsketchdaily",
                             "v0.01",
                             "hbabaran"));
        this.refreshTokenHandler = new RefreshTokenHandler(new AndroidTokenStore(prefs),
                this.redditClient);
        this.am = AuthenticationManager.get();
        this.am.init(this.redditClient, this.refreshTokenHandler);

    }

    public boolean refreshLogin(){
        AuthenticationState state = this.am.checkAuthState();
        System.out.println("resuming submission activity: "+state);
        switch (state) {
            case READY:
                return true;
            case NONE:
                return false;
            case NEED_REFRESH:
                new refreshAccessTokenAsync().execute();
                return true;
        }
        return false;
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


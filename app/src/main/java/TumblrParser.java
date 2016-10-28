/**
 * Created by wren on 28/10/2016.
 */

public class TumblrParser {
    private static final String TUMBLR_OAUTH_CONSUMER_KEY = "SSqgosrC2vc9r4t8eI0OiUL3F9Y9yprIbfM4uaJScEa6dDcj9W";

    //given URL: http://(waxnova.tumblr.com)/post/(152373814265)/inktober-26-box-your-lockpicking-skill-needs_1080x1794
    //turn it into:

    // https://api.tumblr.com/v2/blog/
    //  (waxnova.tumblr.com)
    // /posts/?api_key=
    //  (TUMBLR_OAUTH_CONSUMER_KEY)
    // &id=
    //  (152373814265)

    //use jumblr or don't, its documentation is pretty shit
}

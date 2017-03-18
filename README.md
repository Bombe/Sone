# Sone – The Social Network Plugin for Freenet

Sone aims to provide social network functionality for [Freenet](https://freenetproject.org/) (also here [on GitHub](https://github.com/freenet/)).

## Compiling

Sone’s build process is handled by [Gradle](https://gradle.org/). Just use the Gradle wrapper that comes with Sone:

    # ./gradlew clean build fatJar

This will resolve Sone’s dependencies, compile Sone, run all the tests, and put the file `sone-jar-with-dependencies.jar` into the `build/libs` directory. This is the file that you can load from Freenet’s plugin manager to run Sone.

## Installing

### Prerequisites

For Sone to work you will need a running Freenet node, of course. You will also need the Web of Trust plugin from the official plugins listed on your node’s plugin manager page (*Configuration → Plugins* in the menu).

You will also need a web of trust identity to use Sone. If you do not already have a web of trust identity, select *Community* from the menu, choose “Generate” and follow the instructions on-screen until your identity has been created.

### Loading/Installing

For Sone to work you will need a running Freenet node and the WebOfTrust plugin. Loading Sone happens from Freenet’s web interface using the “Add an Unofficial Plugin from Freenet” section from the node’s plugin manager at *Configuration → Plugins*, at the bottom of the page. Enter the key of the plugin (starting with “USK@”) into the text field and press the “Load” button. The plugin should then be downloaded from Freenet and started once it’s ready.

The node will remember which plugins you loaded so that you don’t need to do that again after your node restarts (e.g. for updates).

## Basic Usage

### Creating a Sone

Now when you select *Sone* from the menu you will be faced with the information that you do not have any Sones yet. Sone does offer you to create a Sone for your web of trust identity, though. Pick the identity you want to use in Sone and hit “Create Sone.”

### Configuring Sone

Choose *Sone → Options* from the menu and see through the list of options. You might want to to activate the “automatically follow new Sones” options so that your post feed does not look so empty. You can also choose to be notified for new Sones, posts, and replies.

In Sone you have the ability to upload a custom avatar for your identity. However, you can not force other people to see it; see the “Avatar Options” section for how the avatar display is controlled.

### Writing Posts

If you choose *Sone* from the menu or simply click the large avatar on the top of the page you will be taken to the “Post Feed” page. It lists your posts as well as posts of all Sones that you follow. You can also write your own posts from here: just click inside the text field at the top of the page, it will expand into a larger box that you can write your post in. (It is also resizable and does not place a limit on the amount of text you want to write.) When you are done, hit the “Post” button right next to it. Your post will then appear at the top of your post feed.

### Replying to Posts

Now, a social network wouldn’t be much fun if you couldn’t talk with other people, and the easiest way to get in touch with them is to reply to one of their posts. To do so, just press the “Comment” link below a post. A text field will appear in which you can simply enter your text. You can press the “+” button on the left side to choose a different identity to post with (in case you have more than one), and once you’re done you hit the “Post Reply” button. The reply will then appear below the post you replied to.

### Liking Posts and Replies

“Liking” is quite the mysterious function. It doesn’t necessarily mean that you literally like something someone said, it is more of a general approval, or a “me too.” That being said, pressing this button has no other consequences than showing other people that you did press this button.

## Advanced Topics

### Addings Links to Posts/Replies

When displaying posts and replies, Sone first parses the text. Special elements, such as Freenet URIs and Sone elements with a special syntax, are replaced with formatting that allow your browser to navigate the elements. Sone recognizes the following elements:

* Links to Freenet URIs are linked to as-is. Make sure to separate the URI from surrounding text by whitespace, such as space or line breaks.
* Links to other Sone’s profiles are added by the prefix “sone://” followed by the ID of the Sone. It is also possible to get the link for a Sone from a post or reply by that Sone; just copy the URL behind the “[link author]” link.
* Links to other posts are added by the prefix “post://” followed by the ID of the post. You can also find the post ID behind the “[link post]” link below a post.

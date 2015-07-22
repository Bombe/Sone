# Sone – The Social Network Plugin for Freenet

Sone aims to provide social network functionality for [Freenet](https://freenetproject.org/) (also here [on GitHub](https://github.com/freenet/)).

## Installing

### Prerequisites

For Sone to work you will need a running Freenet node, of course. You will also need the Web of Trust plugin from the official plugins listed on your node’s plugin manager page (*Configuration → Plugins* in the menu).

If you already have a web of trust identity, you can skip the next section.

You will also need to create an identity in the web of trust. Select *Community* from the menu, choose “Generate” and follow the instructions on-screen until your identity has been created.

### Loading/Installing

For Sone to work you will need a running Freenet node and the WebOfTrust plugin. Loading Sone happens from Freenet’s web interface using the “Add an Unofficial Plugin from Freenet” section from the node’s plugin manager at *Configuration → Plugins*, at the bottom of the page. Enter the key of the plugin (starting with “USK@”) into the text field and press the “Load” button. The plugin should then be downloaded from Freenet and started once it’s ready.

The node will remember which plugins you loaded so that you don’t need to do that again after your node restarts (e.g. for updates).

## Usage

### Creating a Sone

Now when you select *Sone* from the menu you will be faced with the information that you do not have any Sones yet. Sone does offer you to create a Sone for your web of trust identity, though. Pick the identity you want to use in Sone and hit “Create Sone.”

### Configuring Sone

Choose *Sone → Options* from the menu and see through the list of options. You might want to to activate the “automatically follow new Sones” options so that your post feed does not look so empty. You can also choose to be notified for new Sones, posts, and replies.

In Sone you have the ability to upload a custom avatar for your identity. However, you can not force other people to see it; see the “Avatar Options” section for how the avatar display is controlled.

### Writing Posts

If you choose *Sone* from the menu or simply click the large avatar on the top of the page you will be taken to the “Post Feed” page. It lists your posts as well as posts of all Sones that you follow. You can also write your own posts from here: just click inside the text field at the top of the page, it will expand into a larger box that you can write your post in. (It is also resizable and does not place a limit on the amount of text you want to write.) When you are done, hit the “Post” button right next to it. Your post will then appear at the top of your post feed.

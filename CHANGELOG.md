# Changelog

All notable changes to Sone will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/2.0.0/),
but this project does not adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v83] – 2026-10-27

The first release in over six years; many of these changes have already been running on my node for a long time, but I was always too lazy to tie it all up into a new release. Not anymore!

### Added

- Debug logging for unregistering USKs.
- Log decisions for loading/not loading elements.
- Also download files from archives.
- Allow not loading posts or replies that are older than _n_ days, to reduce memory usage.

### Changed

- Update to Gradle 8.14.5.
- Update Fred dependency to build 1501.
- Update dependencies to work with Java 21.
- Reduce dependency on `net.pterodactylus.utils` package.
- Convert more code to Kotlin.
- Refactoring of some tests to rely less on Mockito.
- Mention in web interface that the WebOfTrust plugin might take some time to create identities.
- Improve extraction of text from sites.
- Link parser considers colons as parts of links only if they’re not followed by whitespace.
- Slightly improve styling of page footer.
- Update README.md to reflect changed name of Hyphanet.
- Updated all translations.

### Removed

- Remove FindBugs plugin from build.
- Remove all mentions of Flattr; it’s dead. 🙁

### Fixed

- Broken USK links will no longer be parsed.
- `NullPointerException` during Sone removal.
- Make it possible again to change languages on-the-fly.
- Fix name of test that wouldn’t work on Windows.
- Fix tests failing on non-English systems.
- Fix loading elements when the URL would be too long.
- Fix logging of DefaultElementsLoader by keeping the logger instance alive.
- XML parser is no longer vulnerable to XXE vulnerability. (This is only listed in “Fixed” and not in “Security” because there was never a way to actually exploit it.)

## [v82] – 2020-04-28

### Added

- Add strict filtering for identities from the WebOfTrust.

### Changed

- Move “Save” button on Options page back down to the bottom.
- Update French, Italian translation.

## [v81] – 2020-02-09

This version continues the process of rewriting Sone in Kotlin and has a bit of a focus on notification handling.

### Added

- Show client version in Sone context menu.
- Metrics page shows a bunch of numbers.

### Changed

- Refactor notification handling into dedicated handlers.
- Store elements in database less often.
- Lock Sone if it’s empty on start.
- Upgrade the included jQuery.
- Clean up JavaScript code.
- Update French, German, Spanish, Japanese, Norwegian, Polish, Russian translation.
- When replying to something directed at one of your local Sones, it is now preselected as sender.

### Removed

- Remove trust management buttons.

### Fixed

- Fix formatting of nanosecond times.
- Fix logging in downloader.
- Items in notifications will no longer be duplicated.

## [v80] – 2019-06-27

### Fixed

- Fixed parsing of version string (because the tag now starts with “v”).

## [v79] – 2019-06-26

This version decided to abandon all notion of Semantic Versioning, as it simply does not make sense for an application.

### Changed

- ✨ Started using gitmoji. 😄
- Improve display of parsed USK links.

### Fixed

- Fix search for Sone elements.
- Sone follow time is removed when Sone is not followed anymore.
- Prevent empty names in profile.
- URLs with backlinks are now cut off at the first “/../”.

## [0.9.8] – 2017-11-30

### Added

- Log search times.

### Fixed

- Store profile in Sone after editing.
- Prevent exception when property in identity is null.

## [0.9.7] – 2017-10-07

In this version Kotlin was added to the project, allowing for more expressive code.

### Added

- Recognize Freemail addresses in link parser.
- Load images linked from posts and replies inline.
- Added “Compiling” section to README.

### Changed

- Ignore more punctuation after links.
- Change tooltips for most links parsed from texts.
- Started using Kotlin, replace many tests with Kotlin versions.
- Generate version information during the build.

### Fixed

- Fix album pagination in image gallery.

## [0.9.6] – 2016-10-26

### Changed

- Require Fred build 1475.
- Improve link parser to better handle parenthesis and punctuation after links.

## [0.9.5] – 2016-07-11

### Changed

- Show random image from an album instead of a dedicated album image.
- Update German, Spanish, French translation.

### Fixed

- An empty image title no longer corrupts an album’s state.
- Update timestamps of newly loaded replies.
- Fix formatting of timestamps close to the current time.
- Deleting invalid replies will no longer crash.
- Send Sone removal notification before actually removing Sone.

## [0.9.4] – 2015-11-27

### Added

- Allow loading templates from the filesystem.

### Changed

- Update Spanish translation.
- Remove posts and replies from all notifications when a Sone is removed.

### Fixed

- Don’t use “null” as title for images when title is missing.

## [0.9.3] – 2015-07-22

### Added

- Add README.md.

### Changed

- Log in a way Fred’s logger can handle better.
- Make Sone parsing more robust.

## [0.9.2] – 2015-07-19

### Fixed

- Prevent a NullPointerException when checking if Sones need to be inserted.

## [0.9.1] – 2015-07-18

### Fixed

- Fix homepage link in page footer.

## [0.9] – 2015-07-15

### Added

- Add Spanish translation.

### Changed

- Update Japanese, French, Polish translation.

## [0.9-rc1] – 2015-06-16

### Added

- Use JaCoCo plugin for test coverage information.

### Changed

- Require Fred build 1468.
- Update Japanese translation.
- Remove mention notification if post or reply disappeared.
- Default Sone sort order is now to show most recently active first.
- Set time of insert to current time during the insert.
- Don’t allow empty titles for images and albums.

### Removed

- Rip out db4o from the places it made it into.

### Fixed

- Don’t send events before storing elements.
- Remove posts of a Sone from database correctly.
- Ensure core starts with the correct configuration.
- Trim field names on processing.
- Own identities that do not trust anybody do no longer disappear.
- Own identities without the Sone context are now visible.
- Uploading image would not be allowed for remote Sones anymore. (Not under Security, because it was never exploitable.)
- Stop Sone Rescuers when stopping Sone.

## [0.8.9] – 2014-05-26

### Added

- Tell user about auto-following on an empty Sone feed page.

### Fixed

- Fix link to post for non-JavaScript version of collapsed reply.

## [0.8.8] – 2014-02-27

### Changed

- Update French translation.
- Run profile field values through the filter.
- Increase size of Identicons, let the browser do the scaling.

### Fixed

- Save Sones correctly on shutdown.

## [0.8.7] – 2013-09-25

### Changed

- Updated Japanese, Polish translations.

### Fixed

- Only show “Update Status” input field when logged in.
- Fix Sone Rescue Mode.
- Show album title correctly when deleting an album.
- Fix focusing the input field when clicking a “Comment” link.

## [0.8.6] – 2013-07-05

### Fixed

- Don’t try to show non-existing posts on bookmarks page.
- Fix navigation in image browser.

## [0.8.5] – 2013-06-29

### Added

- Allow filtering of Sones by local/remote status.

### Changed

- Updated French translation.
- Don’t allow creating empty posts and replies.
- Set thread names on downloader threads.

### Fixed

- Set known flag correctly when loading posts and replies.
- Make FCP interface harder to crash.
- Make AJAX interface harder to crash (and easier to debug).
- Fix URL for Fred’s CHECKED_HTTP mechanism.
- Fix avatar selection.
- Encode URLs correctly when creating links to external sites.
- Links containing only a prefix are no longer links.
- Shut down thread pool when stopping Sone.
- Fix marking elements as known.
- Set a Sone’s status correctly after loading.
- Fix querying of Sone following times.
- Fix error when removing reply for unknown post.
- Fix calculation of weeks in timestamps.
- Keep options when rescueing Sone.

## [0.8.4] – 2012-10-13

### Added

- Add Japanese translation.

### Changed

- Make parsing of WebOfTrust FCP replies more robust.

## [0.8.3] – 2012-10-08

### Added

- Add link to homepage to bottom of page.
- Show hint on empty feed page about why it’s empty.
- Updated Polish, Russian, French, Norwegian translations.
- The search page now recognizes some Sone-specific URLs and shortcuts those.

### Changed

- Update WebOfTrust trust values in a background thread.
- Create post excerpts from the parsed representation.
- Save configuration on stopping.

### Removed

- Remove configuration clearing option.

### Fixed

- Fix ampersand character in Identicon URL.
- Show correctly that no image is set as avatar.
- Fix pagination on album pages.
- Fix formatting of timestamps.

## [0.8.2] – 2012-07-14

### Added

- Add Polish translation.

### Changed

- Updated French translation.
- Always collapse all posts on a page.
- Only process new elements from changed notifications.
- Replace single-line input fields with text areas when using without JavaScript.
- Paginate images, make number of images per page configurable.
- Optimize Sone updates by only requesting updates for Sones visible on the page.

### Fixed

- Use correct parameter name for GetIdentitiesByScore command.
- Links to WebOfTrust profile page now contain the ID correctly.

## [0.8.1] – 2012-05-02

### Fixed

- Fix many logging invocations.

## [0.8] – 2012-03-05

### Added

- Add Russian, French, German, and Norwegian translation.
- Add page that shows all new posts and replies.
- Mark replies as known when a post is marked as known.

### Fixed

- Avatars of untrusted Sones would show, even when explicitely not allowed.
- Make “Comment” button translatable.

## [0.7.6] – 2011-12-07

### Added

- Allow using images as avatar.

## [0.7.5] – 2011-11-29

### Changed

- Optimized notification handling.

### Fixed

- Don’t mention removed posts in mention notification.

## [0.7.4] – 2011-11-21

### Added

- Add filter for new/known Sones on “Known Sones” page.
- Sort albums by title in gallery mode.

### Changed

- Only show “Follow” and “Unfollow” buttons on “Known Sones” page if there are any Sones.

### Fixed

- Don’t translate Sone names in the image browser.

## [0.7.3] – 2011-11-16

### Added

- Links to images, Sones, and posts are always allowed by Fred’s content filter.
- Add pagination for bookmarks.
- Allow configuring which notifications should be shown.
- Run album and image description through the parser, allowing links to other things.
- Add buttons to re-order albums and images without JavaScript.
- Add “Next” and “Previous” links to image browser.
- Add link to “All Sones” to image browser.
- Add gallery mode to image browser.

### Changed

- Required Fred build 1405.
- When following a Sone, mark its elements as read.

### Fixed

- Sometimes Sones wouldn’t be inserted over a restart of Sone.
- Detect own identities as removed or added when the Sone context was removed or added. 

## [0.7.2] – 2011-10-04

### Added

- Known Sones can now be sorted by number of images.

### Changed

- Some more links to the WebOfTrust profile page for unknown Sones.
- Make cut-off lengths for posts configurable.
- Improve styling of albums.

### Fixed

- Album images wouldn’t always load correctly.

## [0.7.1] – 2011-09-27

### Fixed

- Fix some bugs in the HTML.

## [0.7] – 2011-09-26

### Added

- Add album and image management.

### Changed

- Only show notification for a reply if its post has been downloaded.

### Fixed

- Ampersand would show up unencoded in Identicon URLs.
- Redirects now work like they should.
- Fix parser missing line breaks in front of sone:// links.

## [0.6.7] – 2011-09-12

### Fixed

- Show correct relative timestamps for replies.

## [0.6.6] – 2011-09-12

### Added

- Allow sorting and filtering Sones on the “Known Sones” page.
- Shorten texts of posts and replies.
- Add context menu for Sones by hovering over the avatar.
- Include number of posts and replies in Sone view.
- Add notification when your Sone is being inserted.
- Link to Web of Trust profile on “View Sone” page for unknown Sones.

### Changed

- Only allow following/unfollowing non-local Sones.
- Improve Sone Rescue mode.
- Redirect to index page if search didn’t turn up anything.
- Add cache for search results.
- Collapse multiple posts in mention notification.
- Don’t parse unknown Sones into links.
- Don’t show line breaks in shortened texts.
- Show “unknown date” for, well, unknown dates.
- Sort Sones in sender selection drop-down.

### Fixed

- Mark replies as known in all tabs.

## [0.6.5] – 2011-06-19

### Added

- Validate options.
- Add FCP interface.
- Add notification for when your Sone is mentioned in a post/reply somewhere.

### Changed

- Don’t mark “click to show” area as new.
- Don’t add recipient in a reply if you’re replying to yourself.
- Remove trailing line breaks from parsed text.
- Posts and replies from yourself are automatically marked as read.
- Force word-wrap in texts by breaking in words.

## [0.6.4] – 2011-05-08

### Added

- Filter texts on insert.
- Show marker when Sone is offline.

### Changed

- Use thread pool to download Sones.
- Use 403 as return code when AJAX requests are from unauthorized clients.

### Fixed

- Fix sending messages to recipients.
- Round times in relative timestamps correctly.
- Update the Sone’s status on View Sone page.

### Security

- Add option to only allow access to Sone for web clients with full access.

## [0.6.3] – 2011-04-28

### Added

- New Sones now automatically follow the official Sone account.
- Strip node prefix from HTTP(s) links, leaving only the key.
- Add link to Sone homepage from `index.html` that’s inserted as part of your Sone.

### Changed

- Always show “mark as read” button in new-element notifications.

### Fixed

- If the identity of a Sone has disappeared from the web of trust, remove the Sone.

## [0.6.2] – 2011-04-17

### Added

- When replying to an element, prefix message with a mention of the author to reply to.
- Update relative timestamps automatically.
- Add permanent links for posts and authors.

### Changed

- Only render notifications if they have changed.
- Improve results of search algorithm.
- Reorder links and timestamp below posts and replies.

### Fixed

- Store auto-follow configuration correctly.
- Only insert posts if we’re on the first page of posts.

## [0.6.1] – 2011-04-10

### Added

- Show posts directed to a Sone on the respective View Sone page.
- Add Sone-specific options.
- Recognize sone:// and post:// links in parser.

### Changed

- The default for negative trust was changed to -25.
- Automatically remove empty notifications.
- Show relative timestamps in web interface.
- Don’t show posts and replies from the future.
- Don’t mark elements as known just by visiting a page.
- Make number of posts shown on a page configurable.

### Fixed

- Search term input field no longer jumps around in Firefox.
- Reset icon animation when there’s no notifications.

## [0.6] – 2011-04-03

### Added

- Add search function.
- Add replies of a Sone to View Sone page.
- Add OpenSearchDescription document to page header.

### Changed

- Collapse multiple empty lines in a parsed text to a single line.
- Disable submit buttons while the request is processed.
- Only show 10 posts per page by default.

## [0.5.2] – 2011-03-29

### Added

- When redirecting to login page, redirect to previous page after logging in.

## [0.5.1] – 2011-03-17

### Added

- Add link to unbookmark all not loaded posts at once.

## [0.5] – 2011-03-08

### Changed

- Update dependency on utils to a non-SNAPSHOT version.

## [0.4.4] – 2011-03-08

### Added

- Allow Sone selection when creating post from a View Sone page.
- Allow bookmarking posts.
- Add “toggle parser” link below posts and replies.

### Changed

- Mark the Sone homepage as a trusted site.
- Change Sone icon and animate it on activity.
- Don’t actively fetch Sones that have been updated more than 7 days ago.
- Sort local Sones in sender selection drop-down.

### Fixed

- Allow negative trust to be set to -100.

## [0.4.3] – 2011-01-26

### Changed

- Show link to homepage in new-version notification.
- Hide post input box when not logged in.

### Fixed

- Prevent elements from being marked as known multiple times.

## [0.4.2] – 2011-01-25

### Added

- Allow selection of origin Sone when posting or replying.
- Allow marking of elements as read from the notification.

### Changed

- Require Fred build 1336.
- Only show trust controls when logged in.
- Don’t show “Comment” link below posts when not logged in.
- Parse profile field values.
- Show text for invalid dates.
- Allow deletion of local elements, regardless of the logged in Sone.
- Remember opened state of notification when replacing it.
- Always be logged in if only one local Sone exists.

### Fixed

- Only set insert time _after_ the insert has finished.

## [0.4.1] – 2011-01-16

### Fixed

- Fix insert of field names.

## [0.4] – 2011-01-15

### Added

- Add custom fields to profiles.
- Add trust management buttons to posts and replies.
- Add link to WebOfTrust page to profile page.

### Changed

- Only make elements as known when they are clicked.
- Require login for most AJAX pages.
- Only send one command to the WebOfTrust plugin at a time.

## [0.3.7] – 2011-01-13

### Added

- Check for updates of Sone and show notification when a new version is found.
- Show current version in page footer.

### Changed

- Make link parser and template rendering more robust.
- Load fewer unnecessary elements in the web interface.
- Don’t load a new post if it’s older than the last post shown.
- Change the way the buttons look.

## [0.3.6.5] – 2011-01-09

### Added

- Store configuration version in configuration.

### Fixed

- Catch more invalid links when rendering.

## [0.3.6.4] – 2011-01-06

### Fixed

- Don’t create links for broken Freenet URIs.

## [0.3.6.3] – 2011-01-06

### Added

- Add protocol version to `sone.xml`.

### Changed

- Hide details in notifications by default.
- Improve display of parsed links.

## [0.3.6.2] – 2010-12-31

### Fixed

- Fix posting again.

## [0.3.6.1] – 2010-12-30

### Fixed

- Fix posting.

## [0.3.6] – 2010-12-30

### Changed

- Require Fred build 1311.
- Show posts directed to you on your index page.

## [0.3.5.1] – 2010-12-18

### Fixed

- Change forgotten version number in plugin as well.

## [0.3.5] – 2010-12-18

### Added

- Show notification if Sone is locked for more than five minutes.

### Changed

- Mark all new elements as known as long as the first start notification is shown.

### Removed

- Don’t use plugin store for configuration anymore.

### Fixed

- Render templates thread-safely.
- Store IDs of liked replies correctly.
- Remove notifications for deleted elements.

## [0.3.4] – 2010-12-12

### Fixed

- Fix incorrect rendering of posts and replies.

## [0.3.3] – 2010-12-12

### Added

- Show notification with some information on the first start of Sone.
- Show notification if configuration cannot be read.

### Changed

- Hide notifications for new elements as long as the first start notification is shown.

## [0.3.2] – 2010-12-09

### Added

- Recognize HTTP(s) links in parser.

### Changed

- Mark posts and replies as known when they are deleted.

### Fixed

- Mark correct element as known in web interface.
- Line breaks are also whitespace when parsing links.
- Replies are now inserted in the correct place on slow connections.

## [0.3.2-rc5] – 2010-12-05

### Added

- Show error when trying to view unknown post.

### Changed

- Load posts and replies more reliably.

### Fixed

- Don’t parse non-existing text.

## [0.3.2-rc4] – 2010-12-02

### Fixed

- Don’t allow posting a message to yourself.
- Don’t create local Sones from POST requests.
- Fixed log output of link parser.

## [0.3.2-rc3] – 2010-11-30

### Changed

- Clear input field when reply was posted.
- Redirect user to index page when trying to log in while already logged in.

## [0.3.2-rc2] – 2010-11-30

### Fixed

- Whitespace detection in links now works on Java 7.
- Store recipient of posts more reliably.

## [0.3.2-rc1] – 2010-11-30

### Added

- Parse post and reply texts for links and format them accordingly.
- Add possibility to write posts to specific Sones.

### Changed

- Don’t hide replies on a post page.
- Load a lot less data when not on the “Known Sones” page.

### Fixed

- Only allow local Sones to log in.
- Post-processing templates now happens after rendering the template.

## [0.3.1] – 2010-11-27

### Fixed

- Only insert new elements on the appropriate pages.
- Insert new elements at the bottom.

## [0.3.1-rc3] – 2010-11-26

### Fixed

- Fix prevention of multiple insertions of posts and replies.
- Fix insertion of new posts and replies.

## [0.3.1-rc2] – 2010-11-26

### Added

- Mark posts and replies as known by clicking on them.

### Fixed

- Don’t change title when Sone is the active tab.

## [0.3.1-rc1] – 2010-11-25

### Added

- Add tooltips to Lock and Unlock buttons.
- Make web interface more interactive with JavaScript.
- Mark tab title in browser when something happens in Sone.

### Changed

- Require Fred build 1305.
- Cache translations in web interface.
- Separate Sone names in tooltip with comma.
- Prefer using local configuration file over using the plugin store.
- Store local configuration as `.properties` and no longer as XML.

## [0.3] – 2010-11-18

### Changed

- Update latest edition of Sones more reliably.
- Improve styling of images with links.

## [0.3-rc1] – 2010-11-17

### Added

- Store client information in Sone.
- Add startup notification that disappears after two minutes.
- Show notifications in web interface.
- Add notification for new Sones.
- Add Sone Rescue mode.
- Add possibility to lock a Sone.

### Fixed

- Persist information about liked elements more reliably.
- Fix wrong Sone assigned to loaded posts and replies.

## [0.2.3] – 2010-11-13

### Added

- Replies can now be created inline.

### Fixed

- Don’t insert Sone if a modification was reverted.

## [0.2.2.1] – 2010-11-12

### Fixed

- Translate default values of fields.

### Security

- Link to internet targets using Fred’s forwarding page.

## [0.2.2] – 2010-11-12

### Added

- Add HTML anchors for posts and replies.
- Add notification for new posts and replies.

### Fixed

- Request translations for the web interface only once.

## [0.2.1] – 2010-11-11

### Added

- Mark Sones, posts, and replies as new in the web interface.
- Persist known Sones over restarts.
- Add Flattr button to page footer.

## [0.2] – 2010-11-10

### Changed

- When a post has more than two replies, only show the latest two.
- Remote Sones now start with a status of “unknown.”

## [0.2-rc4] – 2010-11-10

### Fixed

- Filter CSS class names in web interface.

## [0.2-rc3] – 2010-11-10

### Changed

- Save configuration after every modification.

## [0.2-rc2] – 2010-11-09

### Changed

- Download Sone when the identity was updated in WoT.

### Removed

- Deactivate console logging.

### Fixed

- Don’t block Fred’s USK callback thread.

## [0.2-rc1] – 2010-11-06

### Added

- Use WebOfTrust plugin for identity discovery.
- Use identicons from WebOfTrust for default avatars.
- Add section headers to configuration page.
- Show Sones that liked a post or reply.

### Changed

- Require Fred build 1297.
- Show Sone logo as avatar when not logged in.

### Removed

- Blocking and blacklisting now also uses the WebOfTrust plugin.
- Remove “Help” page.
- Remove backup and key display.
- Don’t show keys of other Sones.

### Fixed

- Add missing page title translations.
- Prevent loading Sone after it was deleted.

## [0.1-rc3] – 2010-10-29

### Fixed

- Fixed creation of Sones from URIs.

## [0.1-rc2] – 2010-10-29

### Added

- Add “Help” page.

### Changed

- Only show Login page in menu when not logged in.

### Fixed

- When counting likes, include all Sones, not only local ones.
- Fix labels for insert and request URIs when creating Sones.

## [0.1-rc1] – 2010-10-27

First public release candidate.


[v83]: https://github.com/Bombe/Sone/compare/v82...v83
[v82]: https://github.com/Bombe/Sone/compare/v81...v82
[v81]: https://github.com/Bombe/Sone/compare/v80...v81
[v80]: https://github.com/Bombe/Sone/compare/v79...v80
[v79]: https://github.com/Bombe/Sone/compare/0.9.8...v79
[0.9.8]: https://github.com/Bombe/Sone/compare/0.9.7...0.9.8
[0.9.7]: https://github.com/Bombe/Sone/compare/0.9.6...0.9.7
[0.9.6]: https://github.com/Bombe/Sone/compare/0.9.5...0.9.6
[0.9.5]: https://github.com/Bombe/Sone/compare/0.9.4...0.9.5
[0.9.4]: https://github.com/Bombe/Sone/compare/0.9.3...0.9.4
[0.9.3]: https://github.com/Bombe/Sone/compare/0.9.2...0.9.3
[0.9.2]: https://github.com/Bombe/Sone/compare/0.9.1...0.9.2
[0.9.1]: https://github.com/Bombe/Sone/compare/0.9...0.9.1
[0.9]: https://github.com/Bombe/Sone/compare/0.9-rc1...0.9
[0.9-rc1]: https://github.com/Bombe/Sone/compare/0.8.9...0.9-rc1
[0.8.9]: https://github.com/Bombe/Sone/compare/0.8.8...0.8.9
[0.8.8]: https://github.com/Bombe/Sone/compare/0.8.7...0.8.8
[0.8.7]: https://github.com/Bombe/Sone/compare/0.8.6...0.8.7
[0.8.6]: https://github.com/Bombe/Sone/compare/0.8.5...0.8.6
[0.8.5]: https://github.com/Bombe/Sone/compare/0.8.4...0.8.5
[0.8.4]: https://github.com/Bombe/Sone/compare/0.8.3...0.8.4
[0.8.3]: https://github.com/Bombe/Sone/compare/0.8.2...0.8.3
[0.8.2]: https://github.com/Bombe/Sone/compare/0.8.1...0.8.2
[0.8.1]: https://github.com/Bombe/Sone/compare/0.8...0.8.1
[0.8]: https://github.com/Bombe/Sone/compare/0.7.6...0.8
[0.7.6]: https://github.com/Bombe/Sone/compare/0.7.5...0.7.6
[0.7.5]: https://github.com/Bombe/Sone/compare/0.7.4...0.7.5
[0.7.4]: https://github.com/Bombe/Sone/compare/0.7.3...0.7.4
[0.7.3]: https://github.com/Bombe/Sone/compare/0.7.2...0.7.3
[0.7.2]: https://github.com/Bombe/Sone/compare/0.7.1...0.7.2
[0.7.1]: https://github.com/Bombe/Sone/compare/0.7...0.7.1
[0.7]: https://github.com/Bombe/Sone/compare/0.6.7...0.7
[0.6.7]: https://github.com/Bombe/Sone/compare/0.6.6...0.6.7
[0.6.6]: https://github.com/Bombe/Sone/compare/0.6.5...0.6.6
[0.6.5]: https://github.com/Bombe/Sone/compare/0.6.4...0.6.5
[0.6.4]: https://github.com/Bombe/Sone/compare/0.6.3...0.6.4
[0.6.3]: https://github.com/Bombe/Sone/compare/0.6.2...0.6.3
[0.6.2]: https://github.com/Bombe/Sone/compare/0.6.1...0.6.2
[0.6.1]: https://github.com/Bombe/Sone/compare/0.6...0.6.1
[0.6]: https://github.com/Bombe/Sone/compare/0.5.2...0.6
[0.5.2]: https://github.com/Bombe/Sone/compare/0.5.1...0.5.2
[0.5.1]: https://github.com/Bombe/Sone/compare/0.5...0.5.1
[0.5]: https://github.com/Bombe/Sone/compare/0.4.4...0.5
[0.4.4]: https://github.com/Bombe/Sone/compare/0.4.3...0.4.4
[0.4.3]: https://github.com/Bombe/Sone/compare/0.4.2...0.4.3
[0.4.2]: https://github.com/Bombe/Sone/compare/0.4.1...0.4.2
[0.4.1]: https://github.com/Bombe/Sone/compare/0.4...0.4.1
[0.4]: https://github.com/Bombe/Sone/compare/0.3.7...0.4
[0.3.7]: https://github.com/Bombe/Sone/compare/0.3.6.5...0.3.7
[0.3.6.5]: https://github.com/Bombe/Sone/compare/0.3.6.4...0.3.6.5
[0.3.6.4]: https://github.com/Bombe/Sone/compare/0.3.6.3...0.3.6.4
[0.3.6.3]: https://github.com/Bombe/Sone/compare/0.3.6.2...0.3.6.3
[0.3.6.2]: https://github.com/Bombe/Sone/compare/0.3.6.1...0.3.6.2
[0.3.6.1]: https://github.com/Bombe/Sone/compare/0.3.6...0.3.6.1
[0.3.6]: https://github.com/Bombe/Sone/compare/0.3.5.1...0.3.6
[0.3.5.1]: https://github.com/Bombe/Sone/compare/0.3.5...0.3.5.1
[0.3.5]: https://github.com/Bombe/Sone/compare/0.3.4...0.3.5
[0.3.4]: https://github.com/Bombe/Sone/compare/0.3.3...0.3.4
[0.3.3]: https://github.com/Bombe/Sone/compare/0.3.2...0.3.3
[0.3.2]: https://github.com/Bombe/Sone/compare/0.3.2-RC5...0.3.2
[0.3.2-RC5]: https://github.com/Bombe/Sone/compare/0.3.2-RC4...0.3.2-RC5
[0.3.2-RC4]: https://github.com/Bombe/Sone/compare/0.3.2-RC3...0.3.2-RC4
[0.3.2-RC3]: https://github.com/Bombe/Sone/compare/0.3.2-RC2...0.3.2-RC3
[0.3.2-RC2]: https://github.com/Bombe/Sone/compare/0.3.2-RC1...0.3.2-RC2
[0.3.2-RC1]: https://github.com/Bombe/Sone/compare/0.3.1...0.3.2-RC1
[0.3.1]: https://github.com/Bombe/Sone/compare/0.3.1-RC3...0.3.1
[0.3.1-RC3]: https://github.com/Bombe/Sone/compare/0.3.1-RC2...0.3.1-RC3
[0.3.1-RC2]: https://github.com/Bombe/Sone/compare/0.3.1-RC1...0.3.1-RC2
[0.3.1-RC1]: https://github.com/Bombe/Sone/compare/0.3...0.3.1-RC1
[0.3]: https://github.com/Bombe/Sone/compare/0.3-RC1...0.3
[0.3-RC1]: https://github.com/Bombe/Sone/compare/0.2.3...0.3-RC1
[0.2.3]: https://github.com/Bombe/Sone/compare/0.2.2.1...0.2.3
[0.2.2.1]: https://github.com/Bombe/Sone/compare/0.2.2...0.2.2.1
[0.2.2]: https://github.com/Bombe/Sone/compare/0.2.1...0.2.2
[0.2.1]: https://github.com/Bombe/Sone/compare/0.2-RC4...0.2.1
[0.2-RC4]: https://github.com/Bombe/Sone/compare/0.2-RC3...0.2-RC4
[0.2-RC3]: https://github.com/Bombe/Sone/compare/0.2...0.2-RC3
[0.2]: https://github.com/Bombe/Sone/compare/0.2-RC2...0.2
[0.2-RC2]: https://github.com/Bombe/Sone/compare/0.2-RC1...0.2-RC2
[0.2-RC1]: https://github.com/Bombe/Sone/compare/0.1-RC3...0.2-RC1
[0.1-RC3]: https://github.com/Bombe/Sone/compare/0.1-RC2...0.1-RC3
[0.1-RC2]: https://github.com/Bombe/Sone/compare/0.1-RC1...0.1-RC2
[0.1-RC1]: https://github.com/Bombe/Sone/releases/tag/0.1-RC1


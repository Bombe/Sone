package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*

@TemplatePath("/templates/invalid.html")
class InvalidPage(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) : SoneTemplatePage("invalid.html", webInterface, loaders, templateRenderer, "Page.Invalid.Title")

@TemplatePath("/templates/noPermission.html")
class NoPermissionPage(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) : SoneTemplatePage("noPermission.html", webInterface, loaders, templateRenderer, "Page.NoPermission.Title")

@TemplatePath("/templates/emptyImageTitle.html")
class EmptyImageTitlePage(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) : SoneTemplatePage("emptyImageTitle.html", webInterface, loaders, templateRenderer, "Page.EmptyImageTitle.Title")

@TemplatePath("/templates/emptyAlbumTitle.html")
class EmptyAlbumTitlePage(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) : SoneTemplatePage("emptyAlbumTitle.html", webInterface, loaders, templateRenderer, "Page.EmptyAlbumTitle.Title")

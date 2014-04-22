this.category.onchange = ->
    path = jsRoutes.controllers.gallery.Galleries.galleries(category.value)['url']
    self.location = path
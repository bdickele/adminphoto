this.category.onchange = ->
    path = jsRoutes.controllers.gallery.Galleries.view(category.value)['url']
    self.location = path
this.folder1.onchange = ->
    path = jsRoutes.controllers.picture.Pictures.pictures(folder1.value, "")['url']
    self.location = path

this.folder2.onchange = ->
    path = jsRoutes.controllers.picture.Pictures.pictures(folder1.value, folder2.value)['url']
    self.location = path

# onchange="self.location = folder1.value"

this.showCarousel = (picToSelect) ->
    divInner = document.getElementById('carouselInner')
    children = divInner.getElementsByTagName('div')
    for child in children
        document.getElementById(child.id).className="item"
    document.getElementById(picToSelect).className="item active"
    $('#modalCarousel').modal()



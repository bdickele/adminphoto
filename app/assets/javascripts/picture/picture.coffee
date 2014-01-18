# Pas convaincu par les liens pour folder1/2.onchange et rescanFolders.onclick
# mais je vois pas comment faire autrement

this.folder1.onchange = ->
    self.location = "/picture/" + folder1.value

this.folder2.onchange = ->
    self.location = "/picture/" + folder1.value + "/" + folder2.value

#this.rescanFolders.onclick = ->
#    self.location = "/picture/refreshFolders"

this.showCarousel = (picToSelect) ->
    divInner = document.getElementById('carouselInner')
    children = divInner.getElementsByTagName('div')
    for child in children
        document.getElementById(child.id).className="item"
    document.getElementById(picToSelect).className="item active"
    $('#modalCarousel').modal()



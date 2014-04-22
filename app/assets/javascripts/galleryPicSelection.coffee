this.folder1.onchange = ->
    path = jsRoutes.controllers.gallery.GalleryPicSelection.pictures(galleryId, folder1.value, "")['url']
    self.location = path

this.folder2.onchange = ->
    path = jsRoutes.controllers.gallery.GalleryPicSelection.pictures(galleryId, folder1.value, folder2.value)['url']
    self.location = path

this.toggleCheckBoxes = (checkToggle) ->
    checkboxes = document.getElementById("picForm").getElementsByTagName('input')
    toggleCheckBox(item, checkToggle) for item in checkboxes
    checkThumbnailClass()

this.toggleCheckBox = (checkBox, checkToggle) ->
    if (checkBox.type == 'checkbox') then checkBox.checked = checkToggle

this.checkThumbnailClass = () ->
    divThumbnails = document.getElementsByName('divThumbnail')
    changeThumbnailClass(divThumbnail) for divThumbnail in divThumbnails

this.changeThumbnailClass = (divThumbnail) ->
    checkBox = divThumbnail.getElementsByTagName('input')[0]
    newClass = if checkBox.checked == true then "thumbnail galleryPicSelected" else "thumbnail"
    divThumbnail.setAttribute("class", newClass)
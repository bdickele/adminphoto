this.folder1.onchange = ->
    path = jsRoutes.controllers.gallery.GalleryPicSelection.view(galleryId, folder1.value, "")['url']
    self.location = path

this.folder2.onchange = ->
    path = jsRoutes.controllers.gallery.GalleryPicSelection.view(galleryId, folder1.value, folder2.value)['url']
    self.location = path


this.toggleCheckBoxes = (checkToggle) ->
    checkboxes = document.getElementById("picForm").getElementsByTagName('input')
    toggleCheckBox(item, checkToggle) for item in checkboxes

this.toggleCheckBox = (checkBox, checkToggle) ->
 if (checkBox.type == 'checkbox') then checkBox.checked = checkToggle
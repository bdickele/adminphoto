# Meme remarque que pour picture.coffee : y'a-t-il un moyen de ne pas taper de façon statique le chemin ?

this.folder1.onchange = ->
    self.location = "/galleryPicSelection/" + galleryId + "/" + folder1.value

this.folder2.onchange = ->
    self.location = "/galleryPicSelection/" + galleryId + "/" + folder1.value + "/" + folder2.value

this.toggleCheckBoxes = (checkToggle) ->
    checkboxes = document.getElementById("picForm").getElementsByTagName('input')
    toggleCheckBox(item, checkToggle) for item in checkboxes

this.toggleCheckBox = (checkBox, checkToggle) ->
 if (checkBox.type == 'checkbox') then checkBox.checked = checkToggle
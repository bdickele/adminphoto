folder1.onchange = ->
    self.location = "/pictures/" + folder1.value

folder2.onchange = ->
    self.location = "/pictures/" + folder1.value + "/" + folder2.value

rescanFolders.onclick = ->
    self.location = "/pictures/refreshFolders"
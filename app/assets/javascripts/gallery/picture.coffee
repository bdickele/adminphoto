# Pas convaincu par les liens pour folder1/2.onchange
# mais je vois pas comment faire autrement

this.folder1.onchange = ->
    self.location = "/galleryPicSelection/" + galleryId + "/" + folder1.value

this.folder2.onchange = ->
    self.location = "/galleryPicSelection/" + galleryId + "/" + folder1.value + "/" + folder2.value




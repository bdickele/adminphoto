package models.gallery


/**
 * Classes used to manage gallery's pictures in creation/edition form
 * Created by bdickele
 * Date: 01/02/14
 */
//TODO TO BE REMOVED
case class GalleryPicsForm(galleryId: Int,
                           galleryTitle: String,
                           thumbnail: String,
                           pictures: List[GalleryPicForm])

case class GalleryPicForm(thumbnail: String,
                          web: String,
                          print: String,
                          description: String)

object GalleryPicsForm {

  def build(galleryPics: GalleryPics): GalleryPicsForm =
    GalleryPicsForm(
      galleryPics.galleryId,
      galleryPics.galleryTitle,
      galleryPics.thumbnail,
      galleryPics.pictures.map(GalleryPicForm(_)))
}

object GalleryPicForm {

  def apply(galleryPic: GalleryPic): GalleryPicForm =
    GalleryPicForm(
      galleryPic.thumbnail,
      galleryPic.web,
      galleryPic.print match {
        case None => ""
        case Some(p) => p
      },
      galleryPic.description match {
        case None => ""
        case Some(s) => s
      })
}
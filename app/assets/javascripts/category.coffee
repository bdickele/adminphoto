categoryId = 0

this.deletionConfirmed = (categoryId) ->
    path = jsRoutes.controllers.category.Categories.deleteCategory(this.categoryId)['url']
    self.location = path

this.askDeletionConfirmation = (categoryId) ->
    this.categoryId = categoryId
    $('#modalPopup').modal()



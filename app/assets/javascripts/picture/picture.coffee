this.showCarousel = (picToSelect) ->
    divInner = document.getElementById('carouselInner')
    children = divInner.getElementsByTagName('div')
    for child in children
        document.getElementById(child.id).className="item"
    document.getElementById(picToSelect).className="item active"
    $('#modalCarousel').modal()



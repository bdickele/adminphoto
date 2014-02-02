//use website

// ********************************************************************************
// Remove existing collections
// ********************************************************************************
db.category.drop()
db.gallery.drop()

// ********************************************************************************
// Creating collections
// ********************************************************************************
db.runCommand({create: "category"})
db.runCommand({create: "gallery"})

// ********************************************************************************
// First inserts
// ********************************************************************************
db.category.insert({
	categoryId: NumberInt(1),
	rank: NumberInt(0),
	title: "2004", 
	comment: "Année 2004",
	online: true
})

db.category.insert({
	categoryId: NumberInt(2),
	rank: NumberInt(1),
	title: "2005",
	comment: "Année 2005",
	online: true
})

db.category.insert({
	categoryId: NumberInt(3),
	rank: NumberInt(2),
	title: "2006",
	comment: "Année 2006",
	online: true
})

db.gallery.insert({
	categoryId: NumberInt(1),
	rank: NumberInt(0),
    galleryId: NumberInt(1),
    date : "2004/6",
    title: "Eté 2004: divers",
    comment: "Ce ne sont pas nos toutes toutes premières photos, mais en tout cas les premières numérisées.",
    thumbnail: "2004/0406_misc/thumbnail/small_0406_Adrien.jpg",
    online: true,
    pictures: [
        {
            thumbnail: "2004/0406_misc/thumbnail/small_0406_Adrien.jpg",
            web: "2004/0406_misc/web/0406_Adrien.jpg",
            comment: "Avec mon fréro"},
        {
            thumbnail: "2004/0406_misc/thumbnail/small_0406_Bastille.jpg",
            web: "2004/0406_misc/web/0406_Bastille.jpg"},
        {
            thumbnail: "2004/0406_misc/thumbnail/small_0406_Beaubourg.jpg",
            web: "2004/0406_misc/web/0406_Beaubourg.jpg"}]
})

// ********************************************************************************
// Indexs
// ********************************************************************************
db.category.ensureIndex({categoryId: 1}, {unique: true})
db.category.ensureIndex({categoryId: 1, rank: -1, title: 1})
db.category.ensureIndex({categoryId: 1, rank: -1, title: 1, online: 1, access: 1})

db.gallery.ensureIndex({galleryId: 1}, {unique: true})
db.gallery.ensureIndex({categoryId: 1, galleryId: 1, rank: -1, title: 1, online: 1, access: 1})


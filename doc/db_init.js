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
	description: "Année 2004", 
	online: true, 
	access: "G"
})

db.category.insert({
	categoryId: NumberInt(2),
	rank: NumberInt(1),
	title: "2005",
	description: "Année 2005",
	online: true,
	access: "G"
})

db.category.insert({
	categoryId: NumberInt(3),
	rank: NumberInt(2),
	title: "2006",
	description: "Année 2006",
	online: true,
	access: "G"
})

db.gallery.insert({
	categoryId: NumberInt(1),
	rank: NumberInt(0),
    galleryId: NumberInt(1),
    date : "2004/6",
    title: "Eté 2004: divers",
    description: "Ce ne sont pas nos toutes toutes premières photos, mais en tout cas les premières numérisées.",
    thumbnail: "2004/0406_misc/thumbnail/small_0406_Versailles_01.jpg",
    online: true,
    access: "G",
    pictures: [
        {
            thumbnail: "2004/0406_misc/small/small_0406_Adrien.jpg",
            web: "2004/0406_misc/web/0406_Adrien.jpg",
            description: ""},
        {
            thumbnail: "2004/0406_misc/small/small_0406_Bastille.jpg",
            web: "2004/0406_misc/web/0406_Bastille.jpg",
            description: ""}]
})

// ********************************************************************************
// Indexs
// ********************************************************************************
db.category.ensureIndex({categoryId: 1}, {unique: true})
db.category.ensureIndex({categoryId: 1, rank: -1, title: 1})
db.category.ensureIndex({categoryId: 1, rank: -1, title: 1, online: 1, access: 1})

db.gallery.ensureIndex({galleryId: 1}, {unique: true})
db.gallery.ensureIndex({categoryId: 1, galleryId: 1, rank: -1, title: 1, online: 1, access: 1})


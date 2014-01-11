use website

// remove existing collection
db.category.drop()
// creating collection
db.runCommand({create: "category"})
// first insert
db.category.insert({
	categoryId: 1, 
	title: "2004", 
	description: "Année 2004", 
	online: true, 
	access: "G", 
	galleries: [{
		galleryId: "200406",
		date : {year: 2004, month: 6},
		title: "Eté 2004: divers",
		description: "Ce ne sont pas nos toutes toutes premières photos, mais en tout cas les premières numérisées.", 
		thumbnail: "2004/0406_misc/thumbnail/small_0406_Versailles_01.jpg",
		online: true,
		access: "G",
		pictures: [
			{folder: "2004/0406_misc",
			thumbnail: "small_0406_Adrien.jpg",
			web: "0406_Adrien.jpg",
			description: ""},
			{folder: "2004/0406_misc",
			thumbnail: "small_0406_Bastille.jpg",
			web: "0406_Bastille.jpg",
			description: ""}]
		}]
})

// indexes
db.category.ensureIndex({categoryId: 1}, {unique: true})
db.category.ensureIndex({categoryId: -1, title: 1, online: 1, access: 1})
db.category.ensureIndex({'galleries.galleryId': 1}, {unique: true})
db.category.ensureIndex({'galleries.galleryId': 1, 'galleries.title': 1, 'galleries.thumbnail': 1, 'galleries.online': 1, 'galleries.access': 1})


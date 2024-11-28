package com.sin_tax.repository
//
//import org.jetbrains.exposed.dao.IntEntity
//import org.jetbrains.exposed.dao.IntEntityClass
//import org.jetbrains.exposed.dao.id.EntityID
//import org.jetbrains.exposed.dao.id.IntIdTable
//
//object Entries : IntIdTable("entries") {
//    var entrant = reference("entrant", Customers)
//    var queue = reference("queue", Queues)
//}
//
//class EntryRepository(id: EntityID<Int>) :IntEntity(id) {
//    companion object: IntEntityClass<EntryRepository>(Entries)
//
//    var entrant by Customers referencedOn Entries.entrant
//    var queue by Queues referencedOn Entries.queue
//}
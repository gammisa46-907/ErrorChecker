package com.gamehouse.doctor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeBaseTest {

    @Test
    fun gamesMatchCatalogOneToOne() {
        assertEquals(15, Games.all.size)
        assertEquals(GameCatalog.all.map { it.id }.toSet(), Games.all.map { it.id }.toSet())
        assertEquals(Games.all.size, Games.all.map { it.id }.toSet().size)
    }

    @Test
    fun everyRiskPointsToAnExistingIssue() {
        Games.all.forEach { g ->
            g.risks.forEach { r -> assertNotNull("${g.id}: isu ${r.issueId} tidak ada", Issues.get(r.issueId)) }
        }
    }

    @Test
    fun issueIdsAreUniqueAndFullyWritten() {
        assertEquals(Issues.all.size, Issues.all.map { it.id }.toSet().size)
        Issues.all.forEach { i ->
            assertTrue("${i.id}: langkah kurang", i.steps.size >= 3)
            listOf(i.title, i.confidence, i.cause, i.evidence, i.signs, i.verify, i.ifFails).forEach { assertTrue("${i.id}: ada bagian kosong", it.isNotBlank()) }
            assertTrue("${i.id}: tanpa gejala", i.symptoms.isNotEmpty())
        }
    }

    @Test
    fun everySymptomHasAtLeastOneIssue() {
        Symptom.values().forEach { s ->
            assertTrue("gejala ${s.name} tanpa penyebab", Issues.all.any { s in it.symptoms })
        }
    }

    @Test
    fun everyGameHasProfileData() {
        Games.all.forEach { g ->
            assertTrue("${g.id}: setup kurang", g.setup.size >= 2)
            assertTrue("${g.id}: kunci registri kosong", g.regKeys.isNotEmpty())
            assertTrue("${g.id}: risiko kurang", g.risks.size >= 4)
            assertTrue("${g.id}: baseline kosong", g.baseline.isNotBlank())
        }
    }

    @Test
    fun academyOfMagicHasFlashAsTopRisk() {
        val top = Games.get("academy_of_magic")!!.risks.first()
        assertEquals("FLASH_ACTIVEX", top.issueId)
        assertEquals(Level.TINGGI, top.level)
    }
}

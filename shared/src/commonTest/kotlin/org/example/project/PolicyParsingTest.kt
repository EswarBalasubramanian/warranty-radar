package org.example.project

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import org.example.project.model.PolicyDraft
import org.example.project.model.PolicyKind
import org.example.project.model.parseFlexibleDateLabel
import org.example.project.model.policyActionHint
import org.example.project.model.policyKindFrom
import org.example.project.model.resolvedEndEpochDay
import org.example.project.scanner.extractPolicyDrafts
import org.example.project.scanner.mergePolicyDrafts
import org.example.project.scanner.parseReceiptGuesses

class PolicyParsingTest {

    @Test
    fun extractsSimpleWarrantyWithDuration() {
        val drafts = extractPolicyDrafts("6 Month Warranty")
        assertEquals(1, drafts.size)
        assertEquals(PolicyKind.Warranty, drafts[0].kind)
        assertEquals(180, drafts[0].durationDays)
    }

    @Test
    fun extractsServiceCentreReplacement() {
        val drafts = extractPolicyDrafts("10 Days Service Centre Replacement")
        assertEquals(1, drafts.size)
        assertEquals(PolicyKind.Replacement, drafts[0].kind)
        assertEquals(10, drafts[0].durationDays)
        assertEquals("Service centre", drafts[0].provider)
    }

    @Test
    fun extractsReplacementWithExplicitDeadline() {
        val drafts = extractPolicyDrafts("Replacement by Amazon until 25 Jul 2026")
        assertEquals(1, drafts.size)
        assertEquals(PolicyKind.Replacement, drafts[0].kind)
        assertEquals("Amazon", drafts[0].provider)
        assertEquals(LocalDate(2026, 7, 25).toEpochDays(), drafts[0].endEpochDay)
    }

    @Test
    fun extractsHyphenatedReturns() {
        val drafts = extractPolicyDrafts("30-day returns")
        assertEquals(1, drafts.size)
        assertEquals(PolicyKind.Return, drafts[0].kind)
        assertEquals(30, drafts[0].durationDays)
    }

    @Test
    fun extractsWordNumberDurations() {
        val drafts = extractPolicyDrafts("Two year guarantee included")
        assertEquals(1, drafts.size)
        assertEquals(PolicyKind.Warranty, drafts[0].kind)
        assertEquals(730, drafts[0].durationDays)
    }

    @Test
    fun capturesNonReturnableAsInformation() {
        val drafts = extractPolicyDrafts("This item is non-returnable")
        assertEquals(1, drafts.size)
        assertEquals(PolicyKind.Return, drafts[0].kind)
        assertNull(drafts[0].durationDays)
        assertNull(drafts[0].endEpochDay)
    }

    @Test
    fun ignoresPageFurniture() {
        val drafts = extractPolicyDrafts("Return to top\nAdd to cart\nSee similar items")
        assertTrue(drafts.isEmpty())
    }

    @Test
    fun splitsBulletSeparatedPolicies() {
        val drafts = extractPolicyDrafts("10 days Replacement • 1 Year Warranty")
        assertEquals(2, drafts.size)
        assertEquals(PolicyKind.Replacement, drafts[0].kind)
        assertEquals(10, drafts[0].durationDays)
        assertEquals(PolicyKind.Warranty, drafts[1].kind)
        assertEquals(365, drafts[1].durationDays)
    }

    @Test
    fun mergesSamePolicyAcrossScans() {
        val firstScan = extractPolicyDrafts("1 Year Manufacturer Warranty")
        val secondScan = extractPolicyDrafts("1 year warranty valid until 3 Jan 2027")
        val merged = mergePolicyDrafts(firstScan, secondScan)
        assertEquals(1, merged.size)
        assertEquals(PolicyKind.Warranty, merged[0].kind)
        assertEquals(365, merged[0].durationDays)
        assertEquals("Manufacturer", merged[0].provider)
        assertEquals(LocalDate(2027, 1, 3).toEpochDays(), merged[0].endEpochDay)
    }

    @Test
    fun keepsDistinctPoliciesSeparateWhenMerging() {
        val merged = mergePolicyDrafts(
            extractPolicyDrafts("10 days Replacement"),
            extractPolicyDrafts("2 Year Warranty")
        )
        assertEquals(2, merged.size)
    }

    @Test
    fun unknownKindDegradesToOther() {
        assertEquals(PolicyKind.Other, policyKindFrom("QuantumCoverage"))
        assertEquals(PolicyKind.Replacement, policyKindFrom("replacement"))
    }

    @Test
    fun parsesFlexibleDateFormats() {
        val expected = LocalDate(2026, 7, 18)
        assertEquals(expected, parseFlexibleDateLabel("18 Jul 2026"))
        assertEquals(expected, parseFlexibleDateLabel("18 July 2026"))
        assertEquals(expected, parseFlexibleDateLabel("July 18, 2026"))
        assertEquals(expected, parseFlexibleDateLabel("18/07/2026"))
        assertEquals(expected, parseFlexibleDateLabel("18-07-26"))
        assertEquals(expected, parseFlexibleDateLabel("2026-07-18"))
        assertNull(parseFlexibleDateLabel("not a date"))
    }

    @Test
    fun resolvesDurationAgainstPurchaseDate() {
        val purchase = LocalDate(2026, 7, 1).toEpochDays()
        val draft = PolicyDraft(kind = PolicyKind.Return, title = "30-day returns", durationDays = 30)
        assertEquals(purchase + 30, resolvedEndEpochDay(draft, purchase))
    }

    @Test
    fun explicitEndDateWinsOverDuration() {
        val purchase = LocalDate(2026, 7, 1).toEpochDays()
        val explicitEnd = LocalDate(2026, 8, 15).toEpochDays()
        val draft = PolicyDraft(kind = PolicyKind.Warranty, title = "Warranty", durationDays = 365, endEpochDay = explicitEnd)
        assertEquals(explicitEnd, resolvedEndEpochDay(draft, purchase))
    }

    @Test
    fun actionHintsMatchPolicyType() {
        assertNotNull(policyActionHint(PolicyKind.Replacement, 2))
        assertNotNull(policyActionHint(PolicyKind.Warranty, 20))
        assertNull(policyActionHint(PolicyKind.Warranty, 200))
        assertNull(policyActionHint(PolicyKind.Return, -1))
        assertNull(policyActionHint(PolicyKind.Return, null))
    }

    @Test
    fun guessesStoreNameSkippingAddressAndPhoneLines() {
        val receipt = parseReceiptGuesses(
            """
            123 Main Street
            (555) 123-4567
            Currys PC World
            Receipt #4471
            Total: ${'$'}349.00
            """.trimIndent()
        )
        assertEquals("Currys PC World", receipt.guessedStore)
    }

    @Test
    fun guessesGrandTotalOverSubtotalAndTax() {
        val receipt = parseReceiptGuesses(
            """
            Best Buy
            Subtotal: ${'$'}299.00
            Tax: ${'$'}24.00
            Grand Total: ${'$'}323.00
            """.trimIndent()
        )
        assertEquals(323.00, receipt.guessedPrice)
    }

    @Test
    fun guessesStoreFromSoldByLabelOnNextLine() {
        val receipt = parseReceiptGuesses(
            """
            Tax Invoice/Bill of Supply/Cash Memo
            Order Date: 06.06.2026
            TOTAL: ₹533.75 ₹3,499.00
            Sold By :
            Clicktech Retail Private Limited
            Bangalore, Karnataka, 562107
            """.trimIndent()
        )
        assertEquals("Clicktech Retail Private Limited", receipt.guessedStore)
    }

    @Test
    fun guessesLargestPriceOnTabularTotalLine() {
        val receipt = parseReceiptGuesses(
            """
            TP-Link AX1500 Wi-Fi 6 Range Extender
            HSN:84717030
            ₹2,965.25 1 ₹2,965.25 18% IGST ₹533.75 ₹3,499.00
            TOTAL: ₹533.75 ₹3,499.00
            """.trimIndent()
        )
        assertEquals(3499.00, receipt.guessedPrice)
    }

    @Test
    fun guessesThousandsSeparatedPriceCorrectlyInsteadOfTruncating() {
        val receipt = parseReceiptGuesses(
            """
            APC Back-UPS BX1100C-IN
            Cashback Upto ${'$'}399.00 cashback as Amazon Pay Balance when...
            -21% ₹7,995
            M.R.P.: ₹10,100
            """.trimIndent()
        )
        assertEquals(10100.0, receipt.guessedPrice)
    }

    @Test
    fun ignoresMediaGalleryBadgeWhenGuessingStoreName() {
        val receipt = parseReceiptGuesses(
            """
            3 VIDEOS
            APC Back-UPS BX1100C-IN 1100VA / 660W UPS
            Visit the APC Store
            """.trimIndent()
        )
        assertNull(receipt.guessedStore)
    }

    @Test
    fun guessesPurchaseDateIgnoringExpiryLine() {
        val receipt = parseReceiptGuesses(
            """
            Best Buy
            Purchase Date: 01/07/2026
            Warranty expires: 01/07/2028
            """.trimIndent()
        )
        assertEquals("01/07/2026", receipt.guessedPurchaseDateLabel)
    }
}

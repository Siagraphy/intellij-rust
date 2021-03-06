/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.lang.core.resolve

import com.intellij.psi.PsiDocumentManager
import org.intellij.lang.annotations.Language
import org.rust.lang.RsTestBase
import org.rust.lang.core.psi.ext.RsNamedElement
import org.rust.lang.core.psi.ext.RsWeakReferenceElement
import org.rust.lang.core.resolve.ref.RsResolveCache
import org.rust.openapiext.Testmark

class RsResolveCacheTest : RsTestBase() {
    private fun RsWeakReferenceElement.checkResolvedTo(marker: String) {
        val resolved = checkedResolve()
        val target = findElementInEditor<RsNamedElement>(marker)

        check(resolved == target) {
            "$this `${this.text}` should resolve to $target, was $resolved instead"
        }
    }

    private fun doTest(@Language("Rust") code: String, textToType: String) {
        InlineFile(code).withCaret()

        val refElement = findElementInEditor<RsWeakReferenceElement>("^")

        refElement.checkResolvedTo("X")

        myFixture.type(textToType)
        PsiDocumentManager.getInstance(project).commitAllDocuments() // process PSI modification events
        check(refElement.isValid)

        refElement.checkResolvedTo("Y")
    }

    private fun doTest(@Language("Rust") code: String, textToType: String, mark: Testmark) = mark.checkHit {
        doTest(code, textToType)
    }

    fun `test`() = doTest("""
        mod a { struct S; }
                     //X
        mod b { struct S; }
                     //Y
        use a/*caret*/::S;
        type T = S;
               //^
    """, "\bb", RsResolveCache.Testmarks.cacheCleared)
}

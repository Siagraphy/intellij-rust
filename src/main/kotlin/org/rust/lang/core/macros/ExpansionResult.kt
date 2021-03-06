/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.lang.core.macros

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.StubBasedPsiElement
import org.rust.lang.core.psi.RsMacroCall
import org.rust.lang.core.psi.ext.RsElement

/**
 *  [ExpansionResult]s are those elements which exist in temporary,
 *  in-memory PSI-files and are injected into real PSI. Their real
 *  parent is this temp PSI-file, but they are seen by the rest of
 *  the plugin as the children of [getContext] element.
 */
interface ExpansionResult : RsElement {
    override fun getContext(): PsiElement?

    companion object {
        fun getContextImpl(psi: ExpansionResult): PsiElement? {
            psi.getUserData(RS_EXPANSION_CONTEXT)?.let { return it }
            if (psi is StubBasedPsiElement<*>) {
                val stub = psi.stub
                if (stub != null) return stub.parentStub.psi as RsElement
            }
            return psi.parent
        }
    }
}

fun ExpansionResult.setContext(context: RsElement) {
    putUserData(RS_EXPANSION_CONTEXT, context)
}

fun ExpansionResult.setExpandedFrom(call: RsMacroCall) {
    putUserData(RS_EXPANSION_MACRO_CALL, call)
}

/** The [RsMacroCall] that expanded to this element or null if this element is not produced by a macro */
val ExpansionResult.expandedFrom: RsMacroCall?
    get() = getUserData(RS_EXPANSION_MACRO_CALL) as RsMacroCall?

val ExpansionResult.expandedFromRecursively: RsMacroCall?
    get() {
        var call: RsMacroCall = expandedFrom ?: return null
        while (true) {
            call = call.expandedFrom ?: break
        }

        return call
    }


private val RS_EXPANSION_CONTEXT = Key.create<RsElement>("org.rust.lang.core.psi.CODE_FRAGMENT_FILE")
private val RS_EXPANSION_MACRO_CALL = Key.create<RsElement>("org.rust.lang.core.psi.RS_EXPANSION_MACRO_CALL")


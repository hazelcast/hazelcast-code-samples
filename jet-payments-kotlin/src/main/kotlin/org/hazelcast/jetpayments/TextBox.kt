package org.hazelcast.jetpayments

/*
 * Class for building very simple text boxes. The class is immutable, and the only
 * way to change it is to create a new instance. The class supports wrapping,
 * centering, and justification. It also supports adding a border around the text.
 * The class is not optimized for performance. It is designed for use in demos and
 * examples. The code is not thread-safe.
 */
internal class TextBox(
    text: List<String>,
    private val textWidth: Int = text.maxOf { tabsToSpaces(it).numCodepoints() },
    private val borderStyle: BorderStyle = BorderStyle.NONE,
) {
    constructor(
        vararg text: String,
        textWidth: Int = text.maxOf { tabsToSpaces(it).numCodepoints() },
        borderStyle: BorderStyle = BorderStyle.NONE
    ) : this(text.toList(), textWidth, borderStyle)

    enum class BorderStyle {
        NONE, SINGLE, THICK, DOUBLE;

        data class BorderChars(
            val vert: String,
            val horz: String,
            val tleft: String,
            val tright: String,
            val bleft: String,
            val bright: String,
        )

        val borderChars: BorderChars
            get() = when (this) {
                SINGLE -> BorderChars("│", "─", "┌", "┐", "└", "┘")
                THICK -> BorderChars("┃", "━", "┏", "┓", "┗", "┛")
                DOUBLE -> BorderChars("║", "═", "╔", "╗", "╚", "╝")
                else -> throw IllegalArgumentException("Invalid border style.")
            }
    }

    private val lines: List<String> = text.map { tabsToSpaces(it) }.also { lines ->
        require(text.isNotEmpty()) { "Text cannot be empty." }
        require(textWidth >= 0) { "Width must be greater than or equal to zero." }
        require(lines.all { it.numCodepoints() <= textWidth }) {
            lines.filter { it.numCodepoints() > textWidth }
                .joinToString("\n") { "'$it'" }.let { badLines ->
                    "Lines are too long for width $textWidth:\n$badLines"
                }
        }
    }

    val width: Int get() = if (borderStyle == BorderStyle.NONE) textWidth else textWidth + 4
    val height: Int get() = if (borderStyle == BorderStyle.NONE) lines.size else lines.size + 2

    fun toStrings() = if (borderStyle == BorderStyle.NONE) lines else boxText()

    fun addBorder(newBorderStyle: BorderStyle = BorderStyle.SINGLE): TextBox {
        return if (newBorderStyle == borderStyle) this
        else TextBox(lines, textWidth, newBorderStyle)
    }

    private fun boxText() = buildList {
        if (borderStyle == BorderStyle.NONE) throw IllegalArgumentException("Border style must be set.")

        val chars = borderStyle.borderChars
        val horz = chars.horz.repeat(textWidth + 2)

        // Top border
        add("${chars.tleft}$horz${chars.tright}")

        // Left border, contents, right border
        lines.mapIndexed { i, str ->
            "${chars.vert} $str${str.pad(textWidth)} ${chars.vert}"
        }.let { addAll(it) }

        // Bottom border
        add("${chars.bleft}$horz${chars.bright}")
    }

    private class FillableLine(val maxLength: Int) {
        private val words = mutableListOf<String>()
        private var lineLength = 0

        override fun toString() = words.joinToString(" ")

        fun tryToAdd(word: String): Boolean {
            require(word.isNotEmpty()) { "Word cannot be empty" }
            require(!word.contains("\\s".toRegex())) { "Word cannot contain whitespace" }

            return if (lineLength + 1 + word.numCodepoints() <= maxLength) {
                words.add(word)
                lineLength += 1 + word.numCodepoints()
                true
            } else false
        }
    }

    /*
     * Take a string and wrap it to the given width.
     */
    fun rewrap(newWidth: Int): TextBox {
        fun getNewLine() = FillableLine(newWidth)
        val wrappedLines = lines.flatMap { line ->
            line.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        }.fold(mutableListOf(getNewLine())) { fillableLines, word ->
            fillableLines.apply {
                val lastLine = fillableLines.last()
                if (!lastLine.tryToAdd(word)) {
                    val newFillableLine = getNewLine()
                    if (!newFillableLine.tryToAdd(word)) {
                        throw IllegalArgumentException("'$word' is longer than $newWidth characters.")
                    }
                    add(newFillableLine)
                }
            }
        }.map { it.toString() }
        return TextBox(wrappedLines, newWidth, borderStyle)
    }

    enum class Bias {
        LEFT, RIGHT, CENTER
    }

    fun justify(
        bias: Bias = Bias.LEFT, widenTo: Int = textWidth
    ): TextBox {
        require(widenTo >= textWidth) { "widenTo must be greater than width." }
        val newTextWidth = widenTo
        val newLines = when (bias) {
            Bias.LEFT -> lines.map { it.padEnd(newTextWidth) }
            Bias.RIGHT -> lines.map { it.padStart(newTextWidth) }
            Bias.CENTER -> lines.map { str ->
                val len = str.numCodepoints()
                val leftpad = (newTextWidth - len) / 2
                str.padStart(leftpad + len).padEnd(newTextWidth)
            }
        }
        return TextBox(newLines, newTextWidth, borderStyle)
    }

    fun center(widenTo: Int = textWidth): TextBox =
        justify(bias = Bias.CENTER, widenTo = widenTo)

    /*
     * The border is normally an attribute, not part of the internal representation
     * until it is rendered. This function removes the attribute and changes the
     * internal representation to include the border.
     */
    private fun absorbBorder(): TextBox {
        if (borderStyle == BorderStyle.NONE) return this
        return TextBox(toStrings(), width, BorderStyle.NONE)
    }

    /*
     * Functions to reposition TextBoxes, or combine two TextBoxes side-by-side, or
     * one below the other. All of these methods will render the border into the
     * internal representation of the text, permanently.
     */

    fun verticalCenter(widenTo: Int): TextBox {
        if (this.height >= widenTo) return this
        val withBorder = this.absorbBorder()
        val spacer = " ".repeat(withBorder.width)
        val top = (widenTo - withBorder.height) / 2
        val bottom = widenTo - withBorder.height - top
        val newLines = List(top) { spacer } + lines + List(bottom) { spacer }
        return TextBox(newLines, withBorder.textWidth, BorderStyle.NONE)
    }

    fun adjoin(vararg others: TextBox, spacer: String = " │ "): TextBox {
        val allBoxes = listOf(this, *others)
        val maxHeight = allBoxes.maxOf { it.height }
        // Center only. No option to top- or bottom-align is provided for now.
        val centered = allBoxes.map { it.absorbBorder().verticalCenter(maxHeight) }
        check(centered.all { it.height == maxHeight }) { "TextBoxes should be the same height after centering." }
        val newTextWidth =
            centered.sumOf { it.width } + spacer.numCodepoints() * (centered.size - 1)

        val newLines = List(maxHeight) { i ->
            centered.joinToString(separator = spacer) { it.lines[i] }
        }

        return TextBox(newLines, newTextWidth, BorderStyle.NONE)
    }

    fun stack(
        other: TextBox,
        addSpacer: Boolean = true,
    ): TextBox {
        val spacerChar = "─"
        val mine = this.absorbBorder()
        val toJoin = other.absorbBorder()
        val newTextWidth = maxOf(mine.textWidth, toJoin.textWidth)
        val newLines =
            if (addSpacer) mine.lines + spacerChar.repeat(newTextWidth) + toJoin.lines
            else mine.lines + toJoin.lines
        return TextBox(
            newLines, newTextWidth, BorderStyle.NONE
        ).center() // Stack always centers; no option to left- or right-align.
    }
}

package no.nav.helsearbeidsgiver.aktor

data class Ident(
    var ident: String? = null,
    var identgruppe: String? = null,
    var gjeldende: Boolean? = null,
)

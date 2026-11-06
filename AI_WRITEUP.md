# AI Usage Write-Up

Student: Kent Orchard
Student Number: ST10258582
Module: PROG7314 / OPSC7312

---

I used GitHub Copilot as a code completion assistant inside Android Studio during
this project. It helped reduce repetitive typing when writing boilerplate Kotlin
such as data class definitions, DAO method signatures, and resource files.

All architecture and logic decisions were my own. The MVVM structure, the choice
to use Room as the offline store, the WorkManager sync approach, and the Firebase
integration were planned and built by me. Copilot assisted with filling in method
bodies once the structure was already defined, in the same way autocomplete works
but with slightly more context.

Areas where I used Copilot suggestions included boilerplate Room entity and DAO
signatures which I then reviewed and adjusted to match my schema, string resource
entries for the Afrikaans and Zulu translations which I verified against reference
material before using, repetitive Composable layout sections where the structure
was already established, and README formatting.

I did not use AI tooling to design the application, make architectural decisions,
write business logic, or debug issues. All testing was done manually on an emulator
and bugs were diagnosed by reading error logs and tracing through the code myself.

Every line of code in this repository is something I understand and can explain.

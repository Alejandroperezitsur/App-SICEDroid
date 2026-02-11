package com.example.marsphotos.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

// ============ ACCESO LOGIN RESPONSE ============

@Serializable
@Root(name = "soap:Envelope", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi"),
    Namespace(reference = "http://www.w3.org/2001/XMLSchema", prefix = "xsd"),
    Namespace(prefix = "soap", reference = "http://schemas.xmlsoap.org/soap/envelope/")
)
data class EnvelopeSobreAcceso(
    @field:Element(name = "soap:Body", required = false)
    @param:Element(name = "soap:Body", required = false)
    val body: BodyAccesoResponse? = null
)

@Serializable
@Root(name = "soap:Body", strict = false)
@NamespaceList(
    Namespace(prefix = "soap", reference = "http://schemas.xmlsoap.org/soap/envelope/"),
    Namespace(reference = "http://tempuri.org/")
)
data class BodyAccesoResponse(
    @Element(name = "accesoLoginResponse", required = false)
    @Namespace(reference = "http://tempuri.org/")
    val accesoLoginResponse: AccesoLoginResponse? = null
)

@Serializable
@Root(name = "accesoLoginResponse", strict = false)
@NamespaceList(
    Namespace(reference = "http://tempuri.org/")
)
data class AccesoLoginResponse(
    @Element(name = "accesoLoginResult", required = false)
    @Namespace(reference = "http://tempuri.org/")
    val accesoLoginResult: String? = null
)

// ============ GET ALUMNO ACADEMICO RESPONSE ============

@Serializable
@Root(name = "soap:Envelope", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi"),
    Namespace(reference = "http://www.w3.org/2001/XMLSchema", prefix = "xsd"),
    Namespace(prefix = "soap", reference = "http://schemas.xmlsoap.org/soap/envelope/")
)
data class EnvelopeSobreAlumno(
    @field:Element(name = "soap:Body", required = false)
    @param:Element(name = "soap:Body", required = false)
    val body: BodyAlumnoResponse? = null
)

@Serializable
@Root(name = "soap:Body", strict = false)
data class BodyAlumnoResponse(
    @Element(name = "getAlumnoAcademicoResponse", required = false)
    val getAlumnoAcademicoResponse: AlumnoAcademicoResponse? = null
)

@Serializable
@Root(name = "getAlumnoAcademicoResponse", strict = false)
data class AlumnoAcademicoResponse(
    @Element(name = "getAlumnoAcademicoResult", required = false)
    val getAlumnoAcademicoResult: String? = null
)
// Clases auxiliares para parsear el DataSet XML del perfil
@Serializable
@Root(name = "DataSet", strict = false)
data class PerfilDataSet(
    @field:Element(name = "Alumno", required = false)
    @param:Element(name = "Alumno", required = false)
    val alumno: AlumnoInfo? = null
)

@Serializable
@Root(name = "Alumno", strict = false)
data class AlumnoInfo(
    @field:Element(name = "Matricula", required = false)
    @param:Element(name = "Matricula", required = false)
    val matricula: String? = null,
    
    @field:Element(name = "Nombre", required = false)
    @param:Element(name = "Nombre", required = false)
    val nombre: String? = null,
    
    @field:Element(name = "Apellidos", required = false)
    @param:Element(name = "Apellidos", required = false)
    val apellidos: String? = null,
    
    @field:Element(name = "Carrera", required = false)
    @param:Element(name = "Carrera", required = false)
    val carrera: String? = null,
    
    @field:Element(name = "Semestre", required = false)
    @param:Element(name = "Semestre", required = false)
    val semestre: String? = null,
    
    @field:Element(name = "Promedio", required = false)
    @param:Element(name = "Promedio", required = false)
    val promedio: String? = null,
    
    @field:Element(name = "Estado", required = false)
    @param:Element(name = "Estado", required = false)
    val estado: String? = null,
    
    @field:Element(name = "StatusMatricula", required = false)
    @param:Element(name = "StatusMatricula", required = false)
    val statusMatricula: String? = null
)
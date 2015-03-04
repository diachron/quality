/* CVS $Id: $ */
package de.unibonn.iai.eis.diachron.semantics.knownvocabs; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from http://www.w3.org/ns/dcat.rdf 
 * @author Auto-generated by schemagen on 04 Mar 2015 14:00 
 */
public class DCAT {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/ns/dcat#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Could be any kind of URL that gives access to a distribution of the dataset. 
     *  E.g. landing page, download, feed URL, SPARQL endpoint. Use when your catalog 
     *  does not have information on which it is or when it is definitely not a download.?????? 
     *  ?? ????? ???????????? ?????? URL ??? ????? ???????? ??? ??????? ???? ??????? 
     *  ?????????. ?.?. ?????????? ??????? ?????????, ???????????, feed URL, ?????? 
     *  ???????? SPARQL. ?? ??????????????? ???? ? ????????? ??? ???????? ??????????? 
     *  ??? ????????? ? ??? ??? ????????????? ??????.?? ???? ???? ?????? ??? ????????. 
     *  ??? ??? ?????? ?? ??? ????? ???? ???? ?????? ?????? ??????? downloadURL???????????????????????????????????SPARQL??????????????????Puede 
     *  ser cualquier tipo de URL que de acceso a una distribuci?n del conjunto de 
     *  datos, e.g., p?gina de aterrizaje, descarga, URL feed, punto de acceso SPARQL. 
     *  Utilizado cuando su cat?logo de datos no tiene informaci?n sobre donde est? 
     *  o cuando no se puede descargarCeci peut ?tre tout type d'URL qui donne acc?s 
     *  ? une distribution du jeu de donn?es. Par exemple, un lien ? une page HTML 
     *  contenant un lien au jeu de donn?es, un Flux RSS, un point d'acc?s SPARQL. 
     *  Utilisez le lorsque votre catalogue ne contient pas d'information sur quoi 
     *  il est ou quand ce n'est pas t?l?chargeable.</p>
     */
    public static final Property accessURL = m_model.createProperty( "http://www.w3.org/ns/dcat#accessURL" );
    
    /** <p>El tama?o de una distribuci?n en bytesLa taille de la distribution en octectsThe 
     *  size of a distribution in bytes.?? ??????? ???? ???????? ?? bytes.?????????????????? 
     *  ?????????</p>
     */
    public static final Property byteSize = m_model.createProperty( "http://www.w3.org/ns/dcat#byteSize" );
    
    /** <p>describe size of resource in bytes. This term has been deprecated</p> */
    public static final Property bytes = m_model.createProperty( "http://www.w3.org/ns/dcat#bytes" );
    
    /** <p>Links a dataset to relevant contact information which is provided using VCard.??????? 
     *  ??? ?????? ????????? ?? ??? ??????? ?????? ????????????, ???? VCard.???? ????? 
     *  ???????? ?????? ????? ???? ???????? VCardRelie un jeu de donn?es ? une information 
     *  de contact utile en utilisant VCard.Enlaza un conjunto de datos a informaci?n 
     *  de contacto relevante utilizando VCard????????VCard???????????????????????????</p>
     */
    public static final Property contactPoint = m_model.createProperty( "http://www.w3.org/ns/dcat#contactPoint" );
    
    /** <p>links a dataset to a dictionary that helps interpreting the data. This term 
     *  has been deprecated</p>
     */
    public static final Property dataDictionary = m_model.createProperty( "http://www.w3.org/ns/dcat#dataDictionary" );
    
    /** <p>describes the quality of data e.g. precision. This should not be used to describe 
     *  the data collection characteristics, other more specialized statistical properties 
     *  can be used instead. This term has been deprecated</p>
     */
    public static final Property dataQuality = m_model.createProperty( "http://www.w3.org/ns/dcat#dataQuality" );
    
    /** <p>Links a catalog to a dataset that is part of the catalog.?????????????????Enlaza 
     *  un cat?logo a un conjunto de datos que es parte de ese cat?logoRelie un catalogue 
     *  ? un jeu de donn?es faisant partie de ce catalogue??????? ???? ???????? ?? 
     *  ??? ?????? ????????? ?? ????? ?????? ???? ?? ???? ????????.???? ?????? ?????? 
     *  ?????? ????</p>
     */
    public static final Property dataset = m_model.createProperty( "http://www.w3.org/ns/dcat#dataset" );
    
    /** <p>Connects a dataset to one of its available distributions.Connecte un jeu de 
     *  donn?es ? des distributions disponibles.??????? ??? ?????? ????????? ?? ??? 
     *  ??? ??? ?????????? ???????? ???.Conecta un conjunto de datos a una de sus 
     *  distribuciones disponibles???? ????? ???????? ?????? ?? ???? ???? ?????? ??? 
     *  ????????????????????????????????</p>
     */
    public static final Property distribution = m_model.createProperty( "http://www.w3.org/ns/dcat#distribution" );
    
    /** <p>This is a direct link to a downloadable file in a given format. E.g. CSV file 
     *  or RDF file. The format is described by the distribution's dc:format and/or 
     *  dcat:mediaTypeEste es un enlace directo a un fichero descargable en un formato 
     *  dado, e.g., fichero CSV o RDF. El formato es descrito por las propiedades 
     *  de la distribuci?n dc:format y/o dcat:mediaTypeCeci est un lien direct ? un 
     *  fichier t?l?chargeable en un format donn?e. Exple fichier CSV ou RDF. Le format 
     *  est d?crit par les propri?t?s de distribution dc:format et/ou dcat:mediaTypedcat:downloadURL?dcat:accessURL?????????????DCAT???????????????????????????accessURL??????????????????????????????????????????????DCAT??dcat:downloadURL?dcat:accessURL???????????????????????? 
     *  ????? ???? ???? ??????. ??? ????? ??? ?????? ???????? ??????? dc:format dcat:mediaType????? 
     *  ???? ????????? ?????? ???????????? ???? ??????? ?? ??? ???????? ?????. ?.?. 
     *  ??? ?????? CSV ? RDF. ? ????? ??????? ???????????? ??? ??? ????????? dc:format 
     *  ?/??? dcat:mediaType ??? ????????</p>
     */
    public static final Property downloadURL = m_model.createProperty( "http://www.w3.org/ns/dcat#downloadURL" );
    
    /** <p>describes the level of granularity of data in a dataset. The granularity can 
     *  be in time, place etc. This term has been deprecated</p>
     */
    public static final Property granularity = m_model.createProperty( "http://www.w3.org/ns/dcat#granularity" );
    
    /** <p>Un mot-cl? ou ?tiquette d?crivant un jeu de donnn?es.??? ????-?????? ? ??? 
     *  ??????? ??? ?????????? ?? ?????? ?????????.???? ??????? ???? ????? ????????????????????????????????A 
     *  keyword or tag describing the dataset.Una palabra clave o etiqueta que describa 
     *  al conjunto de datos.</p>
     */
    public static final Property keyword = m_model.createProperty( "http://www.w3.org/ns/dcat#keyword" );
    
    /** <p>??? ?????????? ?????????? ???? ???? ???????????? (Web browser) ??? ????? ???????? 
     *  ??? ?????? ?????????, ??? ???????? ????? ?/??? ???????????? ???????????.A 
     *  Web page that can be navigated to in a Web browser to gain access to the dataset, 
     *  its distributions and/or additional information.Une page Web accessible par 
     *  un navigateur Web donnant acc?s au jeu de donn?es, ses distributions et/ou 
     *  des informations additionnelles.????????????????????????????????????????????????????????????? 
     *  ?? ???? ?? ?????? ?????? ??? ????? ???????? ?? ??? ??????? ?????? ?????? ???Una 
     *  p?gina Web que puede ser visitada en un explorador Web para tener acceso al 
     *  conjunto de datos, sus distribuciones y/o informaci?n adicional</p>
     */
    public static final Property landingPage = m_model.createProperty( "http://www.w3.org/ns/dcat#landingPage" );
    
    /** <p>This property SHOULD be used when the media type of the distribution is defined 
     *  in IANA, otherwise dct:format MAY be used with different values.Cette propri?t? 
     *  doit ?tre utilis?e quand c'est d?finit le type de m?dia de la distribution 
     *  en IANA, sinon dct:format DOIT ?tre utilis? avec diff?rentes valeurs.??????????????????????IANA??????????????????SHOULD????????????dct:format???????????????MAY??? 
     *  ???????? ???? ?? ?????? ?? ??????????????? ???? ? ????? ????? ???? ???????? 
     *  ????? ????????? ??? IANA, ?????? ? ???????? dct:format ??????? ?? ?????????????? 
     *  ?? ???????????? ?????.??? ??????? ??? ??????? ??? ??? ??? ????? ???? ??? IANAEsta 
     *  propiedad debe ser usada cuando est? definido el tipo de media de la distribuci?n 
     *  en IANA, de otra manera dct:format puede ser utilizado con diferentes valores</p>
     */
    public static final Property mediaType = m_model.createProperty( "http://www.w3.org/ns/dcat#mediaType" );
    
    /** <p>???? ?????? ???? ????Enlaza un cat?logo a sus registros.Relie un catalogue 
     *  ? ses registres??????? ???? ???????? ?? ??? ?????????? ???.Links a catalog 
     *  to its records.????????????????????</p>
     */
    public static final Property record = m_model.createProperty( "http://www.w3.org/ns/dcat#record" );
    
    /** <p>the size of a distribution. This term has been deprecated</p> */
    public static final Property size = m_model.createProperty( "http://www.w3.org/ns/dcat#size" );
    
    /** <p>The main category of the dataset. A dataset can have multiple themes.??????? 
     *  ??????? ?????? ????????. ????? ???????? ???? ?? ???? ???? ?? ????? ????? ????.La 
     *  categor?a principal del conjunto de datos. Un conjunto de datos puede tener 
     *  varios temas.? ????? ????????? ??? ??????? ?????????. ??? ?????? ????????? 
     *  ??????? ?? ???? ???????? ??????.La cat?gorie principale du jeu de donn?es. 
     *  Un jeu de donn?es peut avoir plusieurs th?mes.???????????????????????????????????????</p>
     */
    public static final Property theme = m_model.createProperty( "http://www.w3.org/ns/dcat#theme" );
    
    /** <p>El sistema de organizaci?n del conocimiento utilizado para clasificar conjuntos 
     *  de datos de cat?logos.The knowledge organization system (KOS) used to classify 
     *  catalog's datasets.????? ????????? ????????? ?????? ????? ???????? ??? ??????????????????????????????????????KOS?knowledge 
     *  organization system???? ??????? ????????? ?????? ??? ??????????????? ??? ??? 
     *  ??????????????? ??? ??????? ????????? ??? ?????????.Le systh?me d'ogranisation 
     *  de connaissances utilis? pour classifier les jeux de donn?es du catalogue.</p>
     */
    public static final Property themeTaxonomy = m_model.createProperty( "http://www.w3.org/ns/dcat#themeTaxonomy" );
    
    /** <p>??? ??????????? ??????? ????????????? ???? ??????? ?????????A curated collection 
     *  of metadata about datasetsUne collection ?labor?e de m?tadonn?es sur les jeux 
     *  de donn?esUna colecci?n conservada de metadatos de conjuntos de datos????????????????????????????????????????????? 
     *  ?? ??????? ????? ????????</p>
     */
    public static final Resource Catalog = m_model.createResource( "http://www.w3.org/ns/dcat#Catalog" );
    
    /** <p>Un registro en un cat?logo de datos que describe un solo conjunto de datos.Un 
     *  registre du catalogue ou une entr?e du catalogue, d?crivant un seul jeu de 
     *  donn?es??? ????????? ???? ?????????, ? ????? ?????????? ??? ???????????? ?????? 
     *  ?????????.A record in a data catalog, describing a single dataset.1????????????????????????????</p>
     */
    public static final Resource CatalogRecord = m_model.createResource( "http://www.w3.org/ns/dcat#CatalogRecord" );
    
    /** <p>Une collection de donn?es, publi?e ou ?labor?e par une seule source, et disponible 
     *  pour acc?s ou t?l?chargement dans un ou plusieurs formats????? ?????? ?????? 
     *  ?? ?????? ?? ??? ???? ?? ? ???? ?????? ????? ?? ???????1?????????????????????????1??????????????????????????????A 
     *  collection of data, published or curated by a single source, and available 
     *  for access or download in one or more formatsUna colecci?n de datos, publicados 
     *  o conservados por una ?nica fuente, y disponibles para acceder o descagar 
     *  en uno o m?s formatos??? ??????? ??? ????????, ???????????? ? ??????????? 
     *  ??? ??? ??? ???? ????, ????????? ?? ???? ???????? ? ??????????? ?? ??? ? ???????????? 
     *  ??????</p>
     */
    public static final Resource Dataset = m_model.createResource( "http://www.w3.org/ns/dcat#Dataset" );
    
    /** <p>???????????????????????????????????????????????????????????????????????????????????????????????????????????????CSV?????API?RSS???????????Representa 
     *  una forma disponible y espec?fica a un conjunto de datos. Cada conjunto de 
     *  datos puede estar disponible en formas diferentes, estas formas pueden representar 
     *  formatos diferentes del conjunto de datos o puntos de acceso diferentes.?????????? 
     *  ??? ???????????? ????????? ????? ???? ??????? ?????????. ???? ?????? ????????? 
     *  ?????? ?? ????? ????????? ?? ???????????? ??????, ?? ?????? ????? ?????? ?? 
     *  ???????????? ???????????? ?????? ??????? ? ??????????? ?????? ????????. ???????????? 
     *  ???????? ???????????????? ??? ????????????? ?????? ?????? CSV, ??? API ? ??? 
     *  RSS feed.??? ???? ?????? ???????? ???? ?????? ????. ????? ?????? ?? ???? ?? 
     *  ???? ????? ?????? ? ????? ??????. ??? ???? ?????? ?? ????? ?????? ???? ?? 
     *  ?????? ?????? ??? ???????? ?? ????? ??? ???.Repr?sente une forme sp?cifique 
     *  d'un jeu de donn?es. Caque jeu de donn?es peut ?tre disponible sous diff?rentes 
     *  formes, celles-ci pouvant repr?senter diff?rents formats du jeu de donn?es 
     *  ou diff?rents endpoint. Des exemples de distribution sont des fichirs CSV, 
     *  des API ou des flux RSS.Represents a specific available form of a dataset. 
     *  Each dataset might be available in different forms, these forms might represent 
     *  different formats of the dataset or different endpoints. Examples of distributions 
     *  include a downloadable CSV file, an API or an RSS feed</p>
     */
    public static final Resource Distribution = m_model.createResource( "http://www.w3.org/ns/dcat#Distribution" );
    
    /** <p>represents a downloadable distribution of a dataset. This term has been deprecated</p> */
    public static final Resource Download = m_model.createResource( "http://www.w3.org/ns/dcat#Download" );
    
    /** <p>represents availability of a dataset as a feed. This term has been deprecated</p> */
    public static final Resource Feed = m_model.createResource( "http://www.w3.org/ns/dcat#Feed" );
    
    /** <p>represents a web service that enables access to the data of a dataset. This 
     *  term has been deprecated</p>
     */
    public static final Resource WebService = m_model.createResource( "http://www.w3.org/ns/dcat#WebService" );
    
}

/* CVS $Id: $ */
package de.unibonn.iai.eis.diachron.semantics.knownvocabs; 
import org.apache.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from http://www.w3.org/ns/dcat# 
 * @author Auto-generated by schemagen on 11 Jul 2017 17:35 
 */
public class Dcat {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/ns/dcat#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>أي رابط يتيح الوصول إلى البيانات. إذا كان الرابط هو ربط مباشر لملف يمكن تحميله 
     *  استخدم الخاصية downloadURLΜπορεί να είναι οποιουδήποτε είδους URL που δίνει 
     *  πρόσβαση στη διανομή ενός συνόλου δεδομένων. Π.χ. ιστοσελίδα αρχικής πρόσβασης, 
     *  μεταφόρτωση, feed URL, σημείο διάθεσης SPARQL. Να χρησιμοποιείται όταν ο κατάλογος 
     *  δεν περιέχει πληροφορίες εαν πρόκειται ή όχι για μεταφορτώσιμο αρχείο.Ceci 
     *  peut être tout type d'URL qui donne accès à une distribution du jeu de données. 
     *  Par exemple, un lien à une page HTML contenant un lien au jeu de données, 
     *  un Flux RSS, un point d'accès SPARQL. Utilisez le lorsque votre catalogue 
     *  ne contient pas d'information sur quoi il est ou quand ce n'est pas téléchargeable.Could 
     *  be any kind of URL that gives access to a distribution of the dataset. E.g. 
     *  landing page, download, feed URL, SPARQL endpoint. Use when your catalog does 
     *  not have information on which it is or when it is definitely not a download.Puede 
     *  ser cualquier tipo de URL que de acceso a una distribución del conjunto de 
     *  datos, e.g., página de aterrizaje, descarga, URL feed, punto de acceso SPARQL. 
     *  Utilizado cuando su catálogo de datos no tiene información sobre donde está 
     *  o cuando no se puede descargarデータセットの配信にアクセス権を与えるランディング・ページ、フィード、SPARQLエンドポイント、その他の種類の資源。</p>
     */
    public static final Property accessURL = m_model.createProperty( "http://www.w3.org/ns/dcat#accessURL" );
    
    /** <p>الحجم بالبايتاتEl tamaño de una distribución en bytesLa taille de la distribution 
     *  en octectsΤο μέγεθος μιας διανομής σε bytes.The size of a distribution in 
     *  bytes.バイトによる配信のサイズ。</p>
     */
    public static final Property byteSize = m_model.createProperty( "http://www.w3.org/ns/dcat#byteSize" );
    
    /** <p>describe size of resource in bytes. This term has been deprecated</p> */
    public static final Property bytes = m_model.createProperty( "http://www.w3.org/ns/dcat#bytes" );
    
    /** <p>データセットを、VCardを用いて提供されている適切な連絡先情報にリンクします。Links a dataset to relevant contact 
     *  information which is provided using VCard.Enlaza un conjunto de datos a información 
     *  de contacto relevante utilizando VCardRelie un jeu de données à une information 
     *  de contact utile en utilisant VCard.Συνδέει ένα σύνολο δεδομένων με ένα σχετικό 
     *  σημείο επικοινωνίας, μέσω VCard.تربط قائمة البيانات بعنوان اتصال موصف باستخدام 
     *  VCard</p>
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
    
    /** <p>カタログの一部であるデータセット。تربط الفهرس بقائمة بيانات ضمنهRelie un catalogue à un jeu 
     *  de données faisant partie de ce catalogueLinks a catalog to a dataset that 
     *  is part of the catalog.Συνδέει έναν κατάλογο με ένα σύνολο δεδομένων το οποίο 
     *  ανήκει στον εν λόγω κατάλογο.Enlaza un catálogo a un conjunto de datos que 
     *  es parte de ese catálogo</p>
     */
    public static final Property dataset = m_model.createProperty( "http://www.w3.org/ns/dcat#dataset" );
    
    /** <p>Συνδέει ένα σύνολο δεδομένων με μία από τις διαθέσιμες διανομές του.Connecte 
     *  un jeu de données à des distributions disponibles.تربط قائمة البيانات بطريقة 
     *  أو بشكل يسمح الوصول الى البياناتデータセットを、その利用可能な配信に接続します。Connects a dataset 
     *  to one of its available distributions.Conecta un conjunto de datos a una de 
     *  sus distribuciones disponibles</p>
     */
    public static final Property distribution = m_model.createProperty( "http://www.w3.org/ns/dcat#distribution" );
    
    /** <p>Este es un enlace directo a un fichero descargable en un formato dado, e.g., 
     *  fichero CSV o RDF. El formato es descrito por las propiedades de la distribución 
     *  dc:format y/o dcat:mediaTypeThis is a direct link to a downloadable file in 
     *  a given format. E.g. CSV file or RDF file. The format is described by the 
     *  distribution's dc:format and/or dcat:mediaTypeΕίναι ένας σύνδεσμος άμεσης 
     *  μεταφόρτωσης ενός αρχείου σε μια δεδομένη μορφή. Π.χ. ένα αρχείο CSV ή RDF. 
     *  Η μορφη αρχείου περιγράφεται από τις ιδιότητες dc:format ή/και dcat:mediaType 
     *  της διανομήςCeci est un lien direct à un fichier téléchargeable en un format 
     *  donnée. Exple fichier CSV ou RDF. Le format est décrit par les propriétés 
     *  de distribution dc:format et/ou dcat:mediaTypedcat:downloadURLはdcat:accessURLの特定の形式です。しかし、DCATプロファイルが非ダウンロード・ロケーションに対してのみaccessURLを用いる場合には、より強い分離を課すことを望む可能性があるため、この含意を強化しないように、DCATは、dcat:downloadURLをdcat:accessURLのサブプロパティーであると定義しません。رابط 
     *  مباشر لملف يمكن تحميله. نوع الملف يتم توصيفه باستخدام الخاصية dc:format dcat:mediaType</p>
     */
    public static final Property downloadURL = m_model.createProperty( "http://www.w3.org/ns/dcat#downloadURL" );
    
    /** <p>describes the level of granularity of data in a dataset. The granularity can 
     *  be in time, place etc. This term has been deprecated</p>
     */
    public static final Property granularity = m_model.createProperty( "http://www.w3.org/ns/dcat#granularity" );
    
    /** <p>Μία λέξη-κλειδί ή μία ετικέτα που περιγράφει το σύνολο δεδομένων.كلمة مفتاحيه 
     *  توصف قائمة البياناتUn mot-clé ou étiquette décrivant un jeu de donnnées.データセットを記述しているキーワードまたはタグ。Una 
     *  palabra clave o etiqueta que describa al conjunto de datos.A keyword or tag 
     *  describing the dataset.</p>
     */
    public static final Property keyword = m_model.createProperty( "http://www.w3.org/ns/dcat#keyword" );
    
    /** <p>データセット、その配信および（または）追加情報にアクセスするためにウエブ・ブラウザでナビゲートできるウェブページ。Μία ιστοσελίδα πλοηγίσιμη 
     *  μέσω ενός φυλλομετρητή (Web browser) που δίνει πρόσβαση στο σύνολο δεδομένων, 
     *  τις διανομές αυτού ή/και επιπρόσθετες πληροφορίες.A Web page that can be navigated 
     *  to in a Web browser to gain access to the dataset, its distributions and/or 
     *  additional information.Une page Web accessible par un navigateur Web donnant 
     *  accès au jeu de données, ses distributions et/ou des informations additionnelles.صفحة 
     *  وب يمكن من خلالها الوصول الى قائمة البيانات أو إلى معلومات إضافية متعلقة بهاUna 
     *  página Web que puede ser visitada en un explorador Web para tener acceso al 
     *  conjunto de datos, sus distribuciones y/o información adicional</p>
     */
    public static final Property landingPage = m_model.createProperty( "http://www.w3.org/ns/dcat#landingPage" );
    
    /** <p>Η ιδιότητα αυτή ΘΑ ΠΡΕΠΕΙ να χρησιμοποιείται όταν ο τύπος μέσου μίας διανομής 
     *  είναι ορισμένος στο IANA, αλλιώς η ιδιότητα dct:format ΔΥΝΑΤΑΙ να χρησιμοποιηθεί 
     *  με διαφορετικές τιμές.Esta propiedad debe ser usada cuando está definido el 
     *  tipo de media de la distribución en IANA, de otra manera dct:format puede 
     *  ser utilizado con diferentes valoresこのプロパティーは、配信のメディア・タイプがIANAで定義されているときに使用すべきで（SHOULD）、そうでない場合には、dct:formatを様々な値と共に使用できます（MAY）。يجب 
     *  استخدام هذه الخاصية إذا كان نوع الملف معرف ضمن IANAThis property SHOULD be 
     *  used when the media type of the distribution is defined in IANA, otherwise 
     *  dct:format MAY be used with different values.Cette propriété doit être utilisée 
     *  quand c'est définit le type de média de la distribution en IANA, sinon dct:format 
     *  DOIT être utilisé avec différentes valeurs.</p>
     */
    public static final Property mediaType = m_model.createProperty( "http://www.w3.org/ns/dcat#mediaType" );
    
    /** <p>Relie un catalogue à ses registresتربط الفهرس بسجل ضمنهEnlaza un catálogo 
     *  a sus registros.Συνδέει έναν κατάλογο με τις καταγραφές του.カタログの一部であるカタログ・レコード。Links 
     *  a catalog to its records.</p>
     */
    public static final Property record = m_model.createProperty( "http://www.w3.org/ns/dcat#record" );
    
    /** <p>the size of a distribution. This term has been deprecated</p> */
    public static final Property size = m_model.createProperty( "http://www.w3.org/ns/dcat#size" );
    
    /** <p>The main category of the dataset. A dataset can have multiple themes.データセットの主要カテゴリー。データセットは複数のテーマを持つことができます。التصنيف 
     *  الرئيسي لقائمة البيانات. قائمة البيانات يمكن أن تملك أكثر من تصنيف رئيسي واحد.La 
     *  categoría principal del conjunto de datos. Un conjunto de datos puede tener 
     *  varios temas.La catégorie principale du jeu de données. Un jeu de données 
     *  peut avoir plusieurs thèmes.Η κύρια κατηγορία του συνόλου δεδομένων. Ένα σύνολο 
     *  δεδομένων δύναται να έχει πολλαπλά θέματα.</p>
     */
    public static final Property theme = m_model.createProperty( "http://www.w3.org/ns/dcat#theme" );
    
    /** <p>カタログのデータセットを分類するために用いられる知識組織化体系（KOS；knowledge organization system）。Le systhème 
     *  d'ogranisation de connaissances utilisé pour classifier les jeux de données 
     *  du catalogue.The knowledge organization system (KOS) used to classify catalog's 
     *  datasets.El sistema de organización del conocimiento utilizado para clasificar 
     *  conjuntos de datos de catálogos.لائحة التصنيفات المستخدمه لتصنيف قوائم البيانات 
     *  ضمن الفهرسΤο σύστημα οργάνωσης γνώσης που χρησιμοποιείται για την κατηγοριοποίηση 
     *  των συνόλων δεδομένων του καταλόγου.</p>
     */
    public static final Property themeTaxonomy = m_model.createProperty( "http://www.w3.org/ns/dcat#themeTaxonomy" );
    
    /** <p>データ・カタログは、データセットに関するキュレートされたメタデータの集合です。Una colección conservada de metadatos 
     *  de conjuntos de datosUne collection élaborée de métadonnées sur les jeux de 
     *  donnéesمجموعة من توصيفات قوائم البياناتA curated collection of metadata about 
     *  datasetsΜια επιμελημένη συλλογή μεταδεδομένων περί συνόλων δεδομένων</p>
     */
    public static final Resource Catalog = m_model.createResource( "http://www.w3.org/ns/dcat#Catalog" );
    
    /** <p>1つのデータセットを記述したデータ・カタログ内のレコード。A record in a data catalog, describing a single 
     *  dataset.Μία καταγραφή ενός καταλόγου, η οποία περιγράφει ένα συγκεκριμένο 
     *  σύνολο δεδομένων.Un registro en un catálogo de datos que describe un solo 
     *  conjunto de datos.Un registre du catalogue ou une entrée du catalogue, décrivant 
     *  un seul jeu de données</p>
     */
    public static final Resource CatalogRecord = m_model.createResource( "http://www.w3.org/ns/dcat#CatalogRecord" );
    
    /** <p>A collection of data, published or curated by a single source, and available 
     *  for access or download in one or more formatsΜία συλλογή από δεδομένα, δημοσιευμένη 
     *  ή επιμελημένη από μία και μόνο πηγή, διαθέσιμη δε προς πρόσβαση ή μεταφόρτωση 
     *  σε μία ή περισσότερες μορφές1つのエージェントによって公開またはキュレートされ、1つ以上の形式でアクセスまたはダウンロードできるデータの集合。Une 
     *  collection de données, publiée ou élaborée par une seule source, et disponible 
     *  pour accès ou téléchargement dans un ou plusieurs formatsقائمة بيانات منشورة 
     *  أو مجموعة من قبل مصدر ما و متاح الوصول إليها أو تحميلهاUna colección de datos, 
     *  publicados o conservados por una única fuente, y disponibles para acceder 
     *  o descagar en uno o más formatos</p>
     */
    public static final Resource Dataset = m_model.createResource( "http://www.w3.org/ns/dcat#Dataset" );
    
    /** <p>データセットの特定の利用可能な形式を表わします。各データセットは、異なる形式で利用できることがあり、これらの形式は、データセットの異なる形式や、異なるエンドポイントを表わす可能性があります。配信の例には、ダウンロード可能なCSVファイル、API、RSSフィードが含まれます。شكل 
     *  محدد لقائمة البيانات يمكن الوصول إليه. قائمة بيانات ما يمكن أن تكون متاحه 
     *  باشكال و أنواع متعددة. ملف يمكن تحميله أو واجهة برمجية يمكن من خلالها الوصول 
     *  إلى البيانات هي أمثلة على ذلك.Αναπαριστά μία συγκεκριμένη διαθέσιμη μορφή 
     *  ενός συνόλου δεδομένων. Κάθε σύνολο δεδομενων μπορεί να είναι διαθέσιμο σε 
     *  διαφορετικές μορφές, οι μορφές αυτές μπορεί να αναπαριστούν διαφορετικές μορφές 
     *  αρχείων ή διαφορετικά σημεία διάθεσης. Παραδείγματα διανομών συμπεριλαμβάνουν 
     *  ένα μεταφορτώσιμο αρχείο μορφής CSV, ένα API ή ένα RSS feed.Represents a specific 
     *  available form of a dataset. Each dataset might be available in different 
     *  forms, these forms might represent different formats of the dataset or different 
     *  endpoints. Examples of distributions include a downloadable CSV file, an API 
     *  or an RSS feedRepresenta una forma disponible y específica a un conjunto de 
     *  datos. Cada conjunto de datos puede estar disponible en formas diferentes, 
     *  estas formas pueden representar formatos diferentes del conjunto de datos 
     *  o puntos de acceso diferentes.Représente une forme spécifique d'un jeu de 
     *  données. Caque jeu de données peut être disponible sous différentes formes, 
     *  celles-ci pouvant représenter différents formats du jeu de données ou différents 
     *  endpoint. Des exemples de distribution sont des fichirs CSV, des API ou des 
     *  flux RSS.</p>
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

package uk.ac.ox.oucs.sirlouie;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.ox.oucs.sirlouie.daia.ResponseBean;
import uk.ac.ox.oucs.sirlouie.primo.AlephService;
import uk.ac.ox.oucs.sirlouie.reply.SearObject;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlephServiceTest extends TestCase {

	AlephService service;

	private String nameSpaceURI = "http://www.exlibrisgroup.com/xsd/jaguar/search";

	private String WEBRESOURCE_URL = "http://primo-s-web-2.sers.ox.ac.uk:1701/PrimoWebServices/xservice/getit";


	private String OLIS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			    "<get-item-list>\n" +
			    "  <reply-text>ok</reply-text>\n" +
			    "  <reply-code>0000</reply-code>\n" +
			    "  <items>\n" +
			    "    <item href=\"http://aleph-dev.bodleian.ox.ac.uk:1891/rest-dlf/record/BIB01010276745/items/BLL50010276745000010\">\n" +
			    "      <z30-sub-library-code>BLLCL</z30-sub-library-code>\n" +
			    "      <z30-item-process-status-code/>\n" +
			    "      <z30-item-status-code>01</z30-item-status-code>\n" +
			    "      <z30-collection-code/>\n" +
			    "      <queue/>\n" +
			    "      <z30>\n" +
			    "        <translate-change-active-library>BLL50</translate-change-active-library>\n" +
			    "        <z30-doc-number>010276745</z30-doc-number>\n" +
			    "        <z30-item-sequence>    1.0</z30-item-sequence>\n" +
			    "        <z30-barcode>300646869</z30-barcode>\n" +
			    "        <z30-sub-library>Balliol College Library</z30-sub-library>\n" +
			    "        <z30-material>Book</z30-material>\n" +
			    "        <z30-item-status>Books</z30-item-status>\n" +
			    "        <z30-open-date>19900101</z30-open-date>\n" +
			    "        <z30-update-date>19960820</z30-update-date>\n" +
			    "        <z30-cataloger/>\n" +
			    "        <z30-date-last-return>20121127</z30-date-last-return>\n" +
			    "        <z30-hour-last-return>0914</z30-hour-last-return>\n" +
			    "        <z30-ip-last-return>BLLCL</z30-ip-last-return>\n" +
			    "        <z30-no-loans>012</z30-no-loans>\n" +
			    "        <z30-alpha>L</z30-alpha>\n" +
			    "        <z30-collection/>\n" +
			    "        <z30-call-no-type>8</z30-call-no-type>\n" +
			    "        <z30-call-no>0792 i 009</z30-call-no>\n" +
			    "        <z30-call-no-key>8 0792 i 009</z30-call-no-key>\n" +
			    "        <z30-call-no-2-type/>\n" +
			    "        <z30-call-no-2/>\n" +
			    "        <z30-call-no-2-key/>\n" +
			    "        <z30-description/>\n" +
			    "        <z30-note-opac/>\n" +
			    "        <z30-note-circulation/>\n" +
			    "        <z30-note-internal/>\n" +
			    "        <z30-order-number/>\n" +
			    "        <z30-inventory-number/>\n" +
			    "        <z30-inventory-number-date>00000000</z30-inventory-number-date>\n" +
			    "        <z30-last-shelf-report-date>00000000</z30-last-shelf-report-date>\n" +
			    "        <z30-price/>\n" +
			    "        <z30-shelf-report-number/>\n" +
			    "        <z30-on-shelf-date>00000000</z30-on-shelf-date>\n" +
			    "        <z30-on-shelf-seq>000000</z30-on-shelf-seq>\n" +
			    "        <z30-doc-number-2>000000000</z30-doc-number-2>\n" +
			    "        <z30-schedule-sequence-2>00000</z30-schedule-sequence-2>\n" +
			    "        <z30-copy-sequence-2>00000</z30-copy-sequence-2>\n" +
			    "        <z30-vendor-code/>\n" +
			    "        <z30-invoice-number/>\n" +
			    "        <z30-line-number>00000</z30-line-number>\n" +
			    "        <z30-pages/>\n" +
			    "        <z30-issue-date>00000000</z30-issue-date>\n" +
			    "        <z30-expected-arrival-date>00000000</z30-expected-arrival-date>\n" +
			    "        <z30-arrival-date>19960820</z30-arrival-date>\n" +
			    "        <z30-item-statistic/>\n" +
			    "        <z30-item-process-status>Not in Process</z30-item-process-status>\n" +
			    "        <z30-copy-id/>\n" +
			    "        <z30-hol-doc-number>000779509</z30-hol-doc-number>\n" +
			    "        <z30-temp-location>No</z30-temp-location>\n" +
			    "        <z30-enumeration-a/>\n" +
			    "        <z30-enumeration-b/>\n" +
			    "        <z30-enumeration-c/>\n" +
			    "        <z30-enumeration-d/>\n" +
			    "        <z30-enumeration-e/>\n" +
			    "        <z30-enumeration-f/>\n" +
			    "        <z30-enumeration-g/>\n" +
			    "        <z30-enumeration-h/>\n" +
			    "        <z30-chronological-i/>\n" +
			    "        <z30-chronological-j/>\n" +
			    "        <z30-chronological-k/>\n" +
			    "        <z30-chronological-l/>\n" +
			    "        <z30-chronological-m/>\n" +
			    "        <z30-supp-index-o/>\n" +
			    "        <z30-85x-type/>\n" +
			    "        <z30-depository-id/>\n" +
			    "        <z30-linking-number>000000000</z30-linking-number>\n" +
			    "        <z30-gap-indicator/>\n" +
			    "        <z30-maintenance-count>002</z30-maintenance-count>\n" +
			    "        <z30-process-status-date>00000000</z30-process-status-date>\n" +
			    "        <z30-upd-time-stamp>200001011200000</z30-upd-time-stamp>\n" +
			    "        <z30-ip-last-return-v6/>\n" +
			    "      </z30>\n" +
			    "      <z13>\n" +
			    "        <translate-change-active-library>BLL50</translate-change-active-library>\n" +
			    "        <z13-doc-number>010276745</z13-doc-number>\n" +
			    "        <z13-year>1989</z13-year>\n" +
			    "        <z13-open-date>20110714</z13-open-date>\n" +
			    "        <z13-update-date>20140927</z13-update-date>\n" +
			    "        <z13-call-no-key/>\n" +
			    "        <z13-call-no-code>05000</z13-call-no-code>\n" +
			    "        <z13-call-no>DB2241.H38 A5 1989b</z13-call-no>\n" +
			    "        <z13-author-code>1001</z13-author-code>\n" +
			    "        <z13-author>Havel, Václav.</z13-author>\n" +
			    "        <z13-title-code/>\n" +
			    "        <z13-title>Living in truth : twenty-two essays published on the occasion of the award of the Erasmus Prize to V</z13-title>\n" +
			    "        <z13-imprint-code>260</z13-imprint-code>\n" +
			    "        <z13-imprint>London : Faber, 1989.</z13-imprint>\n" +
			    "        <z13-isbn-issn-code>020</z13-isbn-issn-code>\n" +
			    "        <z13-isbn-issn>0571144403</z13-isbn-issn>\n" +
			    "        <z13-upd-time-stamp>201312170902505</z13-upd-time-stamp>\n" +
			    "      </z13>\n" +
			    "      <status>Available</status>\n" +
			    "    </item>\n" +
			    "    <item href=\"http://aleph-dev.bodleian.ox.ac.uk:1891/rest-dlf/record/BIB01010276745/items/BOD50010276745000020\">\n" +
			    "      <z30-sub-library-code>BODBL</z30-sub-library-code>\n" +
			    "      <z30-item-process-status-code/>\n" +
			    "      <z30-item-status-code>01</z30-item-status-code>\n" +
			    "      <z30-collection-code>BODGL</z30-collection-code>\n" +
			    "      <queue/>\n" +
			    "      <z30>\n" +
			    "        <translate-change-active-library>BOD50</translate-change-active-library>\n" +
			    "        <z30-doc-number>010276745</z30-doc-number>\n" +
			    "        <z30-item-sequence>    2.0</z30-item-sequence>\n" +
			    "        <z30-barcode>400617949</z30-barcode>\n" +
			    "        <z30-sub-library>Bodleian Library</z30-sub-library>\n" +
			    "        <z30-material>Book</z30-material>\n" +
			    "        <z30-item-status>Books</z30-item-status>\n" +
			    "        <z30-open-date>19900101</z30-open-date>\n" +
			    "        <z30-update-date>19990716</z30-update-date>\n" +
			    "        <z30-cataloger>bod</z30-cataloger>\n" +
			    "        <z30-date-last-return>00000000</z30-date-last-return>\n" +
			    "        <z30-hour-last-return>0000</z30-hour-last-return>\n" +
			    "        <z30-ip-last-return/>\n" +
			    "        <z30-no-loans>000</z30-no-loans>\n" +
			    "        <z30-alpha>L</z30-alpha>\n" +
			    "        <z30-collection>Lower Gladstone Link Open Shelves</z30-collection>\n" +
			    "        <z30-call-no-type>8</z30-call-no-type>\n" +
			    "        <z30-call-no>(UBHU) M90.G02296</z30-call-no>\n" +
			    "        <z30-call-no-key>0000008 ubhu m0000090 g0002296</z30-call-no-key>\n" +
			    "        <z30-call-no-2-type/>\n" +
			    "        <z30-call-no-2/>\n" +
			    "        <z30-call-no-2-key/>\n" +
			    "        <z30-description/>\n" +
			    "        <z30-note-opac/>\n" +
			    "        <z30-note-circulation/>\n" +
			    "        <z30-note-internal/>\n" +
			    "        <z30-order-number/>\n" +
			    "        <z30-inventory-number/>\n" +
			    "        <z30-inventory-number-date>00000000</z30-inventory-number-date>\n" +
			    "        <z30-last-shelf-report-date>00000000</z30-last-shelf-report-date>\n" +
			    "        <z30-price/>\n" +
			    "        <z30-shelf-report-number/>\n" +
			    "        <z30-on-shelf-date>00000000</z30-on-shelf-date>\n" +
			    "        <z30-on-shelf-seq>000000</z30-on-shelf-seq>\n" +
			    "        <z30-doc-number-2>000000000</z30-doc-number-2>\n" +
			    "        <z30-schedule-sequence-2>00000</z30-schedule-sequence-2>\n" +
			    "        <z30-copy-sequence-2>00000</z30-copy-sequence-2>\n" +
			    "        <z30-vendor-code/>\n" +
			    "        <z30-invoice-number/>\n" +
			    "        <z30-line-number>00000</z30-line-number>\n" +
			    "        <z30-pages/>\n" +
			    "        <z30-issue-date>00000000</z30-issue-date>\n" +
			    "        <z30-expected-arrival-date>00000000</z30-expected-arrival-date>\n" +
			    "        <z30-arrival-date>19990716</z30-arrival-date>\n" +
			    "        <z30-item-statistic/>\n" +
			    "        <z30-item-process-status>Not in Process</z30-item-process-status>\n" +
			    "        <z30-copy-id/>\n" +
			    "        <z30-hol-doc-number>000779510</z30-hol-doc-number>\n" +
			    "        <z30-temp-location>No</z30-temp-location>\n" +
			    "        <z30-enumeration-a/>\n" +
			    "        <z30-enumeration-b/>\n" +
			    "        <z30-enumeration-c/>\n" +
			    "        <z30-enumeration-d/>\n" +
			    "        <z30-enumeration-e/>\n" +
			    "        <z30-enumeration-f/>\n" +
			    "        <z30-enumeration-g/>\n" +
			    "        <z30-enumeration-h/>\n" +
			    "        <z30-chronological-i/>\n" +
			    "        <z30-chronological-j/>\n" +
			    "        <z30-chronological-k/>\n" +
			    "        <z30-chronological-l/>\n" +
			    "        <z30-chronological-m/>\n" +
			    "        <z30-supp-index-o/>\n" +
			    "        <z30-85x-type/>\n" +
			    "        <z30-depository-id/>\n" +
			    "        <z30-linking-number>000000000</z30-linking-number>\n" +
			    "        <z30-gap-indicator/>\n" +
			    "        <z30-maintenance-count>000</z30-maintenance-count>\n" +
			    "        <z30-process-status-date>00000000</z30-process-status-date>\n" +
			    "        <z30-upd-time-stamp>200001011200000</z30-upd-time-stamp>\n" +
			    "        <z30-ip-last-return-v6/>\n" +
			    "      </z30>\n" +
			    "      <z13>\n" +
			    "        <translate-change-active-library>BOD50</translate-change-active-library>\n" +
			    "        <z13-doc-number>010276745</z13-doc-number>\n" +
			    "        <z13-year>1989</z13-year>\n" +
			    "        <z13-open-date>20110714</z13-open-date>\n" +
			    "        <z13-update-date>20140927</z13-update-date>\n" +
			    "        <z13-call-no-key/>\n" +
			    "        <z13-call-no-code>05000</z13-call-no-code>\n" +
			    "        <z13-call-no>DB2241.H38 A5 1989b</z13-call-no>\n" +
			    "        <z13-author-code>1001</z13-author-code>\n" +
			    "        <z13-author>Havel, Václav.</z13-author>\n" +
			    "        <z13-title-code/>\n" +
			    "        <z13-title>Living in truth : twenty-two essays published on the occasion of the award of the Erasmus Prize to V</z13-title>\n" +
			    "        <z13-imprint-code>260</z13-imprint-code>\n" +
			    "        <z13-imprint>London : Faber, 1989.</z13-imprint>\n" +
			    "        <z13-isbn-issn-code>020</z13-isbn-issn-code>\n" +
			    "        <z13-isbn-issn>0571144403</z13-isbn-issn>\n" +
			    "        <z13-upd-time-stamp>201312170902505</z13-upd-time-stamp>\n" +
			    "      </z13>\n" +
			    "      <status>Available</status>\n" +
			    "    </item>\n" +
			    "    <item href=\"http://aleph-dev.bodleian.ox.ac.uk:1891/rest-dlf/record/BIB01010276745/items/CAT50010276745000010\">\n" +
			    "      <z30-sub-library-code>CATCL</z30-sub-library-code>\n" +
			    "      <z30-item-process-status-code/>\n" +
			    "      <z30-item-status-code>05</z30-item-status-code>\n" +
			    "      <z30-collection-code/>\n" +
			    "      <queue/>\n" +
			    "      <z30>\n" +
			    "        <translate-change-active-library>CAT50</translate-change-active-library>\n" +
			    "        <z30-doc-number>010276745</z30-doc-number>\n" +
			    "        <z30-item-sequence>    1.0</z30-item-sequence>\n" +
			    "        <z30-barcode>307058386</z30-barcode>\n" +
			    "        <z30-sub-library>St Catherine's Coll Library</z30-sub-library>\n" +
			    "        <z30-material>Book</z30-material>\n" +
			    "        <z30-item-status>Loans</z30-item-status>\n" +
			    "        <z30-open-date>20121008</z30-open-date>\n" +
			    "        <z30-update-date>20121008</z30-update-date>\n" +
			    "        <z30-cataloger>GROMOVAL</z30-cataloger>\n" +
			    "        <z30-date-last-return>00000000</z30-date-last-return>\n" +
			    "        <z30-hour-last-return>0000</z30-hour-last-return>\n" +
			    "        <z30-ip-last-return/>\n" +
			    "        <z30-no-loans>000</z30-no-loans>\n" +
			    "        <z30-alpha>L</z30-alpha>\n" +
			    "        <z30-collection/>\n" +
			    "        <z30-call-no-type>8</z30-call-no-type>\n" +
			    "        <z30-call-no>949.2 HAV</z30-call-no>\n" +
			    "        <z30-call-no-key>8 949 2 hav</z30-call-no-key>\n" +
			    "        <z30-call-no-2-type/>\n" +
			    "        <z30-call-no-2/>\n" +
			    "        <z30-call-no-2-key/>\n" +
			    "        <z30-description/>\n" +
			    "        <z30-note-opac/>\n" +
			    "        <z30-note-circulation/>\n" +
			    "        <z30-note-internal/>\n" +
			    "        <z30-order-number/>\n" +
			    "        <z30-inventory-number/>\n" +
			    "        <z30-inventory-number-date>00000000</z30-inventory-number-date>\n" +
			    "        <z30-last-shelf-report-date>00000000</z30-last-shelf-report-date>\n" +
			    "        <z30-price/>\n" +
			    "        <z30-shelf-report-number/>\n" +
			    "        <z30-on-shelf-date>00000000</z30-on-shelf-date>\n" +
			    "        <z30-on-shelf-seq>000000</z30-on-shelf-seq>\n" +
			    "        <z30-doc-number-2>000000000</z30-doc-number-2>\n" +
			    "        <z30-schedule-sequence-2>00000</z30-schedule-sequence-2>\n" +
			    "        <z30-copy-sequence-2>00000</z30-copy-sequence-2>\n" +
			    "        <z30-vendor-code/>\n" +
			    "        <z30-invoice-number/>\n" +
			    "        <z30-line-number>00000</z30-line-number>\n" +
			    "        <z30-pages/>\n" +
			    "        <z30-issue-date>00000000</z30-issue-date>\n" +
			    "        <z30-expected-arrival-date>00000000</z30-expected-arrival-date>\n" +
			    "        <z30-arrival-date>00000000</z30-arrival-date>\n" +
			    "        <z30-item-statistic/>\n" +
			    "        <z30-item-process-status>Not in Process</z30-item-process-status>\n" +
			    "        <z30-copy-id/>\n" +
			    "        <z30-hol-doc-number>011853960</z30-hol-doc-number>\n" +
			    "        <z30-temp-location>No</z30-temp-location>\n" +
			    "        <z30-enumeration-a/>\n" +
			    "        <z30-enumeration-b/>\n" +
			    "        <z30-enumeration-c/>\n" +
			    "        <z30-enumeration-d/>\n" +
			    "        <z30-enumeration-e/>\n" +
			    "        <z30-enumeration-f/>\n" +
			    "        <z30-enumeration-g/>\n" +
			    "        <z30-enumeration-h/>\n" +
			    "        <z30-chronological-i/>\n" +
			    "        <z30-chronological-j/>\n" +
			    "        <z30-chronological-k/>\n" +
			    "        <z30-chronological-l/>\n" +
			    "        <z30-chronological-m/>\n" +
			    "        <z30-supp-index-o/>\n" +
			    "        <z30-85x-type/>\n" +
			    "        <z30-depository-id/>\n" +
			    "        <z30-linking-number>000000000</z30-linking-number>\n" +
			    "        <z30-gap-indicator/>\n" +
			    "        <z30-maintenance-count>000</z30-maintenance-count>\n" +
			    "        <z30-process-status-date>20121008</z30-process-status-date>\n" +
			    "        <z30-upd-time-stamp>200001011200000</z30-upd-time-stamp>\n" +
			    "        <z30-ip-last-return-v6/>\n" +
			    "      </z30>\n" +
			    "      <z13>\n" +
			    "        <translate-change-active-library>CAT50</translate-change-active-library>\n" +
			    "        <z13-doc-number>010276745</z13-doc-number>\n" +
			    "        <z13-year>1989</z13-year>\n" +
			    "        <z13-open-date>20110714</z13-open-date>\n" +
			    "        <z13-update-date>20140927</z13-update-date>\n" +
			    "        <z13-call-no-key/>\n" +
			    "        <z13-call-no-code>05000</z13-call-no-code>\n" +
			    "        <z13-call-no>DB2241.H38 A5 1989b</z13-call-no>\n" +
			    "        <z13-author-code>1001</z13-author-code>\n" +
			    "        <z13-author>Havel, Václav.</z13-author>\n" +
			    "        <z13-title-code/>\n" +
			    "        <z13-title>Living in truth : twenty-two essays published on the occasion of the award of the Erasmus Prize to V</z13-title>\n" +
			    "        <z13-imprint-code>260</z13-imprint-code>\n" +
			    "        <z13-imprint>London : Faber, 1989.</z13-imprint>\n" +
			    "        <z13-isbn-issn-code>020</z13-isbn-issn-code>\n" +
			    "        <z13-isbn-issn>0571144403</z13-isbn-issn>\n" +
			    "        <z13-upd-time-stamp>201312170902505</z13-upd-time-stamp>\n" +
			    "      </z13>\n" +
			    "      <status>Available</status>\n" +
			    "    </item>\n" +
			    "    <item href=\"http://aleph-dev.bodleian.ox.ac.uk:1891/rest-dlf/record/BIB01010276745/items/HER50010276745000010\">\n" +
			    "      <z30-sub-library-code>HERCL</z30-sub-library-code>\n" +
			    "      <z30-item-process-status-code/>\n" +
			    "      <z30-item-status-code>05</z30-item-status-code>\n" +
			    "      <z30-collection-code/>\n" +
			    "      <queue/>\n" +
			    "      <z30>\n" +
			    "        <translate-change-active-library>HER50</translate-change-active-library>\n" +
			    "        <z30-doc-number>010276745</z30-doc-number>\n" +
			    "        <z30-item-sequence>    1.0</z30-item-sequence>\n" +
			    "        <z30-barcode>305250254</z30-barcode>\n" +
			    "        <z30-sub-library>Hertford College Library</z30-sub-library>\n" +
			    "        <z30-material>Book</z30-material>\n" +
			    "        <z30-item-status>Loans</z30-item-status>\n" +
			    "        <z30-open-date>20030326</z30-open-date>\n" +
			    "        <z30-update-date>20030326</z30-update-date>\n" +
			    "        <z30-cataloger>WalkerL</z30-cataloger>\n" +
			    "        <z30-date-last-return>00000000</z30-date-last-return>\n" +
			    "        <z30-hour-last-return>0000</z30-hour-last-return>\n" +
			    "        <z30-ip-last-return/>\n" +
			    "        <z30-no-loans>000</z30-no-loans>\n" +
			    "        <z30-alpha>L</z30-alpha>\n" +
			    "        <z30-collection/>\n" +
			    "        <z30-call-no-type>8</z30-call-no-type>\n" +
			    "        <z30-call-no>B 80/HAV</z30-call-no>\n" +
			    "        <z30-call-no-key>0000008 b 0000080 hav</z30-call-no-key>\n" +
			    "        <z30-call-no-2-type/>\n" +
			    "        <z30-call-no-2/>\n" +
			    "        <z30-call-no-2-key/>\n" +
			    "        <z30-description/>\n" +
			    "        <z30-note-opac/>\n" +
			    "        <z30-note-circulation/>\n" +
			    "        <z30-note-internal/>\n" +
			    "        <z30-order-number/>\n" +
			    "        <z30-inventory-number/>\n" +
			    "        <z30-inventory-number-date>20140715</z30-inventory-number-date>\n" +
			    "        <z30-last-shelf-report-date>20140715</z30-last-shelf-report-date>\n" +
			    "        <z30-price/>\n" +
			    "        <z30-shelf-report-number>her2014-000000011</z30-shelf-report-number>\n" +
			    "        <z30-on-shelf-date>20140715</z30-on-shelf-date>\n" +
			    "        <z30-on-shelf-seq>001127</z30-on-shelf-seq>\n" +
			    "        <z30-doc-number-2>000000000</z30-doc-number-2>\n" +
			    "        <z30-schedule-sequence-2>00000</z30-schedule-sequence-2>\n" +
			    "        <z30-copy-sequence-2>00000</z30-copy-sequence-2>\n" +
			    "        <z30-vendor-code/>\n" +
			    "        <z30-invoice-number/>\n" +
			    "        <z30-line-number>00000</z30-line-number>\n" +
			    "        <z30-pages/>\n" +
			    "        <z30-issue-date>00000000</z30-issue-date>\n" +
			    "        <z30-expected-arrival-date>00000000</z30-expected-arrival-date>\n" +
			    "        <z30-arrival-date>20030326</z30-arrival-date>\n" +
			    "        <z30-item-statistic/>\n" +
			    "        <z30-item-process-status>Not in Process</z30-item-process-status>\n" +
			    "        <z30-copy-id/>\n" +
			    "        <z30-hol-doc-number>000779511</z30-hol-doc-number>\n" +
			    "        <z30-temp-location>No</z30-temp-location>\n" +
			    "        <z30-enumeration-a/>\n" +
			    "        <z30-enumeration-b/>\n" +
			    "        <z30-enumeration-c/>\n" +
			    "        <z30-enumeration-d/>\n" +
			    "        <z30-enumeration-e/>\n" +
			    "        <z30-enumeration-f/>\n" +
			    "        <z30-enumeration-g/>\n" +
			    "        <z30-enumeration-h/>\n" +
			    "        <z30-chronological-i/>\n" +
			    "        <z30-chronological-j/>\n" +
			    "        <z30-chronological-k/>\n" +
			    "        <z30-chronological-l/>\n" +
			    "        <z30-chronological-m/>\n" +
			    "        <z30-supp-index-o/>\n" +
			    "        <z30-85x-type/>\n" +
			    "        <z30-depository-id/>\n" +
			    "        <z30-linking-number>000000000</z30-linking-number>\n" +
			    "        <z30-gap-indicator/>\n" +
			    "        <z30-maintenance-count>000</z30-maintenance-count>\n" +
			    "        <z30-process-status-date>00000000</z30-process-status-date>\n" +
			    "        <z30-upd-time-stamp>201407151027085</z30-upd-time-stamp>\n" +
			    "        <z30-ip-last-return-v6/>\n" +
			    "      </z30>\n" +
			    "      <z13>\n" +
			    "        <translate-change-active-library>HER50</translate-change-active-library>\n" +
			    "        <z13-doc-number>010276745</z13-doc-number>\n" +
			    "        <z13-year>1989</z13-year>\n" +
			    "        <z13-open-date>20110714</z13-open-date>\n" +
			    "        <z13-update-date>20140927</z13-update-date>\n" +
			    "        <z13-call-no-key/>\n" +
			    "        <z13-call-no-code>05000</z13-call-no-code>\n" +
			    "        <z13-call-no>DB2241.H38 A5 1989b</z13-call-no>\n" +
			    "        <z13-author-code>1001</z13-author-code>\n" +
			    "        <z13-author>Havel, Václav.</z13-author>\n" +
			    "        <z13-title-code/>\n" +
			    "        <z13-title>Living in truth : twenty-two essays published on the occasion of the award of the Erasmus Prize to V</z13-title>\n" +
			    "        <z13-imprint-code>260</z13-imprint-code>\n" +
			    "        <z13-imprint>London : Faber, 1989.</z13-imprint>\n" +
			    "        <z13-isbn-issn-code>020</z13-isbn-issn-code>\n" +
			    "        <z13-isbn-issn>0571144403</z13-isbn-issn>\n" +
			    "        <z13-upd-time-stamp>201312170902505</z13-upd-time-stamp>\n" +
			    "      </z13>\n" +
			    "      <status>Available</status>\n" +
			    "    </item>\n" +
			    "    <item href=\"http://aleph-dev.bodleian.ox.ac.uk:1891/rest-dlf/record/BIB01010276745/items/OUS50010276745000010\">\n" +
			    "      <z30-sub-library-code>OUSNU</z30-sub-library-code>\n" +
			    "      <z30-item-process-status-code/>\n" +
			    "      <z30-item-status-code>01</z30-item-status-code>\n" +
			    "      <z30-collection-code>OUSOL</z30-collection-code>\n" +
			    "      <queue/>\n" +
			    "      <z30>\n" +
			    "        <translate-change-active-library>OUS50</translate-change-active-library>\n" +
			    "        <z30-doc-number>010276745</z30-doc-number>\n" +
			    "        <z30-item-sequence>    1.0</z30-item-sequence>\n" +
			    "        <z30-barcode>305363900</z30-barcode>\n" +
			    "        <z30-sub-library>Oxford Union Society Library*</z30-sub-library>\n" +
			    "        <z30-material>Book</z30-material>\n" +
			    "        <z30-item-status>Books</z30-item-status>\n" +
			    "        <z30-open-date>20040507</z30-open-date>\n" +
			    "        <z30-update-date>20100329</z30-update-date>\n" +
			    "        <z30-cataloger>CooperA</z30-cataloger>\n" +
			    "        <z30-date-last-return>20141029</z30-date-last-return>\n" +
			    "        <z30-hour-last-return>1239</z30-hour-last-return>\n" +
			    "        <z30-ip-last-return>OUSNU</z30-ip-last-return>\n" +
			    "        <z30-no-loans>008</z30-no-loans>\n" +
			    "        <z30-alpha>L</z30-alpha>\n" +
			    "        <z30-collection>Old Library</z30-collection>\n" +
			    "        <z30-call-no-type>8</z30-call-no-type>\n" +
			    "        <z30-call-no>891.864 HAV LIV</z30-call-no>\n" +
			    "        <z30-call-no-key>8 891 864 hav liv</z30-call-no-key>\n" +
			    "        <z30-call-no-2-type/>\n" +
			    "        <z30-call-no-2/>\n" +
			    "        <z30-call-no-2-key/>\n" +
			    "        <z30-description/>\n" +
			    "        <z30-note-opac/>\n" +
			    "        <z30-note-circulation/>\n" +
			    "        <z30-note-internal/>\n" +
			    "        <z30-order-number/>\n" +
			    "        <z30-inventory-number/>\n" +
			    "        <z30-inventory-number-date>00000000</z30-inventory-number-date>\n" +
			    "        <z30-last-shelf-report-date>00000000</z30-last-shelf-report-date>\n" +
			    "        <z30-price/>\n" +
			    "        <z30-shelf-report-number/>\n" +
			    "        <z30-on-shelf-date>00000000</z30-on-shelf-date>\n" +
			    "        <z30-on-shelf-seq>000000</z30-on-shelf-seq>\n" +
			    "        <z30-doc-number-2>000000000</z30-doc-number-2>\n" +
			    "        <z30-schedule-sequence-2>00000</z30-schedule-sequence-2>\n" +
			    "        <z30-copy-sequence-2>00000</z30-copy-sequence-2>\n" +
			    "        <z30-vendor-code/>\n" +
			    "        <z30-invoice-number/>\n" +
			    "        <z30-line-number>00000</z30-line-number>\n" +
			    "        <z30-pages/>\n" +
			    "        <z30-issue-date>00000000</z30-issue-date>\n" +
			    "        <z30-expected-arrival-date>00000000</z30-expected-arrival-date>\n" +
			    "        <z30-arrival-date>20040507</z30-arrival-date>\n" +
			    "        <z30-item-statistic/>\n" +
			    "        <z30-item-process-status>Not in Process</z30-item-process-status>\n" +
			    "        <z30-copy-id/>\n" +
			    "        <z30-hol-doc-number>000779512</z30-hol-doc-number>\n" +
			    "        <z30-temp-location>No</z30-temp-location>\n" +
			    "        <z30-enumeration-a/>\n" +
			    "        <z30-enumeration-b/>\n" +
			    "        <z30-enumeration-c/>\n" +
			    "        <z30-enumeration-d/>\n" +
			    "        <z30-enumeration-e/>\n" +
			    "        <z30-enumeration-f/>\n" +
			    "        <z30-enumeration-g/>\n" +
			    "        <z30-enumeration-h/>\n" +
			    "        <z30-chronological-i/>\n" +
			    "        <z30-chronological-j/>\n" +
			    "        <z30-chronological-k/>\n" +
			    "        <z30-chronological-l/>\n" +
			    "        <z30-chronological-m/>\n" +
			    "        <z30-supp-index-o/>\n" +
			    "        <z30-85x-type/>\n" +
			    "        <z30-depository-id/>\n" +
			    "        <z30-linking-number>000000000</z30-linking-number>\n" +
			    "        <z30-gap-indicator/>\n" +
			    "        <z30-maintenance-count>002</z30-maintenance-count>\n" +
			    "        <z30-process-status-date>00000000</z30-process-status-date>\n" +
			    "        <z30-upd-time-stamp>201410291239069</z30-upd-time-stamp>\n" +
			    "        <z30-ip-last-return-v6/>\n" +
			    "      </z30>\n" +
			    "      <z13>\n" +
			    "        <translate-change-active-library>OUS50</translate-change-active-library>\n" +
			    "        <z13-doc-number>010276745</z13-doc-number>\n" +
			    "        <z13-year>1989</z13-year>\n" +
			    "        <z13-open-date>20110714</z13-open-date>\n" +
			    "        <z13-update-date>20140927</z13-update-date>\n" +
			    "        <z13-call-no-key/>\n" +
			    "        <z13-call-no-code>05000</z13-call-no-code>\n" +
			    "        <z13-call-no>DB2241.H38 A5 1989b</z13-call-no>\n" +
			    "        <z13-author-code>1001</z13-author-code>\n" +
			    "        <z13-author>Havel, Václav.</z13-author>\n" +
			    "        <z13-title-code/>\n" +
			    "        <z13-title>Living in truth : twenty-two essays published on the occasion of the award of the Erasmus Prize to V</z13-title>\n" +
			    "        <z13-imprint-code>260</z13-imprint-code>\n" +
			    "        <z13-imprint>London : Faber, 1989.</z13-imprint>\n" +
			    "        <z13-isbn-issn-code>020</z13-isbn-issn-code>\n" +
			    "        <z13-isbn-issn>0571144403</z13-isbn-issn>\n" +
			    "        <z13-upd-time-stamp>201312170902505</z13-upd-time-stamp>\n" +
			    "      </z13>\n" +
			    "      <status>Available</status>\n" +
			    "    </item>\n" +
			    "    <item href=\"http://aleph-dev.bodleian.ox.ac.uk:1891/rest-dlf/record/BIB01010276745/items/SOM50010276745000010\">\n" +
			    "      <z30-sub-library-code>SOMCL</z30-sub-library-code>\n" +
			    "      <z30-item-process-status-code/>\n" +
			    "      <z30-item-status-code>05</z30-item-status-code>\n" +
			    "      <z30-collection-code/>\n" +
			    "      <queue/>\n" +
			    "      <z30>\n" +
			    "        <translate-change-active-library>SOM50</translate-change-active-library>\n" +
			    "        <z30-doc-number>010276745</z30-doc-number>\n" +
			    "        <z30-item-sequence>    1.0</z30-item-sequence>\n" +
			    "        <z30-barcode>303573744</z30-barcode>\n" +
			    "        <z30-sub-library>Somerville College Library</z30-sub-library>\n" +
			    "        <z30-material>Book</z30-material>\n" +
			    "        <z30-item-status>Loans</z30-item-status>\n" +
			    "        <z30-open-date>20000705</z30-open-date>\n" +
			    "        <z30-update-date>20000705</z30-update-date>\n" +
			    "        <z30-cataloger>PopperC</z30-cataloger>\n" +
			    "        <z30-date-last-return>20141205</z30-date-last-return>\n" +
			    "        <z30-hour-last-return>0949</z30-hour-last-return>\n" +
			    "        <z30-ip-last-return>SOMCL</z30-ip-last-return>\n" +
			    "        <z30-no-loans>004</z30-no-loans>\n" +
			    "        <z30-alpha>L</z30-alpha>\n" +
			    "        <z30-collection/>\n" +
			    "        <z30-call-no-type>8</z30-call-no-type>\n" +
			    "        <z30-call-no>819 HAV 1</z30-call-no>\n" +
			    "        <z30-call-no-key>8 819 hav 1</z30-call-no-key>\n" +
			    "        <z30-call-no-2-type/>\n" +
			    "        <z30-call-no-2/>\n" +
			    "        <z30-call-no-2-key/>\n" +
			    "        <z30-description/>\n" +
			    "        <z30-note-opac/>\n" +
			    "        <z30-note-circulation/>\n" +
			    "        <z30-note-internal/>\n" +
			    "        <z30-order-number/>\n" +
			    "        <z30-inventory-number/>\n" +
			    "        <z30-inventory-number-date>20120727</z30-inventory-number-date>\n" +
			    "        <z30-last-shelf-report-date>20140723</z30-last-shelf-report-date>\n" +
			    "        <z30-price/>\n" +
			    "        <z30-shelf-report-number>som2014-000000019</z30-shelf-report-number>\n" +
			    "        <z30-on-shelf-date>20140821</z30-on-shelf-date>\n" +
			    "        <z30-on-shelf-seq>001364</z30-on-shelf-seq>\n" +
			    "        <z30-doc-number-2>000000000</z30-doc-number-2>\n" +
			    "        <z30-schedule-sequence-2>00000</z30-schedule-sequence-2>\n" +
			    "        <z30-copy-sequence-2>00000</z30-copy-sequence-2>\n" +
			    "        <z30-vendor-code/>\n" +
			    "        <z30-invoice-number/>\n" +
			    "        <z30-line-number>00000</z30-line-number>\n" +
			    "        <z30-pages/>\n" +
			    "        <z30-issue-date>00000000</z30-issue-date>\n" +
			    "        <z30-expected-arrival-date>00000000</z30-expected-arrival-date>\n" +
			    "        <z30-arrival-date>20000705</z30-arrival-date>\n" +
			    "        <z30-item-statistic/>\n" +
			    "        <z30-item-process-status>Not in Process</z30-item-process-status>\n" +
			    "        <z30-copy-id/>\n" +
			    "        <z30-hol-doc-number>000779514</z30-hol-doc-number>\n" +
			    "        <z30-temp-location>No</z30-temp-location>\n" +
			    "        <z30-enumeration-a/>\n" +
			    "        <z30-enumeration-b/>\n" +
			    "        <z30-enumeration-c/>\n" +
			    "        <z30-enumeration-d/>\n" +
			    "        <z30-enumeration-e/>\n" +
			    "        <z30-enumeration-f/>\n" +
			    "        <z30-enumeration-g/>\n" +
			    "        <z30-enumeration-h/>\n" +
			    "        <z30-chronological-i/>\n" +
			    "        <z30-chronological-j/>\n" +
			    "        <z30-chronological-k/>\n" +
			    "        <z30-chronological-l/>\n" +
			    "        <z30-chronological-m/>\n" +
			    "        <z30-supp-index-o/>\n" +
			    "        <z30-85x-type/>\n" +
			    "        <z30-depository-id/>\n" +
			    "        <z30-linking-number>000000000</z30-linking-number>\n" +
			    "        <z30-gap-indicator/>\n" +
			    "        <z30-maintenance-count>001</z30-maintenance-count>\n" +
			    "        <z30-process-status-date>00000000</z30-process-status-date>\n" +
			    "        <z30-upd-time-stamp>201412050949479</z30-upd-time-stamp>\n" +
			    "        <z30-ip-last-return-v6/>\n" +
			    "      </z30>\n" +
			    "      <z13>\n" +
			    "        <translate-change-active-library>SOM50</translate-change-active-library>\n" +
			    "        <z13-doc-number>010276745</z13-doc-number>\n" +
			    "        <z13-year>1989</z13-year>\n" +
			    "        <z13-open-date>20110714</z13-open-date>\n" +
			    "        <z13-update-date>20140927</z13-update-date>\n" +
			    "        <z13-call-no-key/>\n" +
			    "        <z13-call-no-code>05000</z13-call-no-code>\n" +
			    "        <z13-call-no>DB2241.H38 A5 1989b</z13-call-no>\n" +
			    "        <z13-author-code>1001</z13-author-code>\n" +
			    "        <z13-author>Havel, Václav.</z13-author>\n" +
			    "        <z13-title-code/>\n" +
			    "        <z13-title>Living in truth : twenty-two essays published on the occasion of the award of the Erasmus Prize to V</z13-title>\n" +
			    "        <z13-imprint-code>260</z13-imprint-code>\n" +
			    "        <z13-imprint>London : Faber, 1989.</z13-imprint>\n" +
			    "        <z13-isbn-issn-code>020</z13-isbn-issn-code>\n" +
			    "        <z13-isbn-issn>0571144403</z13-isbn-issn>\n" +
			    "        <z13-upd-time-stamp>201312170902505</z13-upd-time-stamp>\n" +
			    "      </z13>\n" +
			    "      <status>Available</status>\n" +
			    "    </item>\n" +
			    "    <item href=\"http://aleph-dev.bodleian.ox.ac.uk:1891/rest-dlf/record/BIB01010276745/items/BOD50010276745000010\">\n" +
			    "      <z30-sub-library-code>SSLBL</z30-sub-library-code>\n" +
			    "      <z30-item-process-status-code/>\n" +
			    "      <z30-item-status-code>01</z30-item-status-code>\n" +
			    "      <z30-collection-code/>\n" +
			    "      <queue/>\n" +
			    "      <z30>\n" +
			    "        <translate-change-active-library>BOD50</translate-change-active-library>\n" +
			    "        <z30-doc-number>010276745</z30-doc-number>\n" +
			    "        <z30-item-sequence>    1.0</z30-item-sequence>\n" +
			    "        <z30-barcode>300122297</z30-barcode>\n" +
			    "        <z30-sub-library>Social Science Library</z30-sub-library>\n" +
			    "        <z30-material>Book</z30-material>\n" +
			    "        <z30-item-status>Books</z30-item-status>\n" +
			    "        <z30-open-date>19900101</z30-open-date>\n" +
			    "        <z30-update-date>20030603</z30-update-date>\n" +
			    "        <z30-cataloger>MildonE</z30-cataloger>\n" +
			    "        <z30-date-last-return>20141126</z30-date-last-return>\n" +
			    "        <z30-hour-last-return>0855</z30-hour-last-return>\n" +
			    "        <z30-ip-last-return>SSLBL</z30-ip-last-return>\n" +
			    "        <z30-no-loans>032</z30-no-loans>\n" +
			    "        <z30-alpha>L</z30-alpha>\n" +
			    "        <z30-collection/>\n" +
			    "        <z30-call-no-type>0</z30-call-no-type>\n" +
			    "        <z30-call-no>DB2241.VAC</z30-call-no>\n" +
			    "        <z30-call-no-key>0 db#2241 vac 0</z30-call-no-key>\n" +
			    "        <z30-call-no-2-type/>\n" +
			    "        <z30-call-no-2/>\n" +
			    "        <z30-call-no-2-key/>\n" +
			    "        <z30-description/>\n" +
			    "        <z30-note-opac/>\n" +
			    "        <z30-note-circulation/>\n" +
			    "        <z30-note-internal/>\n" +
			    "        <z30-order-number/>\n" +
			    "        <z30-inventory-number/>\n" +
			    "        <z30-inventory-number-date>20070718</z30-inventory-number-date>\n" +
			    "        <z30-last-shelf-report-date>00000000</z30-last-shelf-report-date>\n" +
			    "        <z30-price/>\n" +
			    "        <z30-shelf-report-number/>\n" +
			    "        <z30-on-shelf-date>00000000</z30-on-shelf-date>\n" +
			    "        <z30-on-shelf-seq>000000</z30-on-shelf-seq>\n" +
			    "        <z30-doc-number-2>000000000</z30-doc-number-2>\n" +
			    "        <z30-schedule-sequence-2>00000</z30-schedule-sequence-2>\n" +
			    "        <z30-copy-sequence-2>00000</z30-copy-sequence-2>\n" +
			    "        <z30-vendor-code/>\n" +
			    "        <z30-invoice-number/>\n" +
			    "        <z30-line-number>00000</z30-line-number>\n" +
			    "        <z30-pages/>\n" +
			    "        <z30-issue-date>00000000</z30-issue-date>\n" +
			    "        <z30-expected-arrival-date>00000000</z30-expected-arrival-date>\n" +
			    "        <z30-arrival-date>20030603</z30-arrival-date>\n" +
			    "        <z30-item-statistic/>\n" +
			    "        <z30-item-process-status>Not in Process</z30-item-process-status>\n" +
			    "        <z30-copy-id/>\n" +
			    "        <z30-hol-doc-number>000779513</z30-hol-doc-number>\n" +
			    "        <z30-temp-location>No</z30-temp-location>\n" +
			    "        <z30-enumeration-a/>\n" +
			    "        <z30-enumeration-b/>\n" +
			    "        <z30-enumeration-c/>\n" +
			    "        <z30-enumeration-d/>\n" +
			    "        <z30-enumeration-e/>\n" +
			    "        <z30-enumeration-f/>\n" +
			    "        <z30-enumeration-g/>\n" +
			    "        <z30-enumeration-h/>\n" +
			    "        <z30-chronological-i/>\n" +
			    "        <z30-chronological-j/>\n" +
			    "        <z30-chronological-k/>\n" +
			    "        <z30-chronological-l/>\n" +
			    "        <z30-chronological-m/>\n" +
			    "        <z30-supp-index-o/>\n" +
			    "        <z30-85x-type/>\n" +
			    "        <z30-depository-id/>\n" +
			    "        <z30-linking-number>000000000</z30-linking-number>\n" +
			    "        <z30-gap-indicator/>\n" +
			    "        <z30-maintenance-count>006</z30-maintenance-count>\n" +
			    "        <z30-process-status-date>00000000</z30-process-status-date>\n" +
			    "        <z30-upd-time-stamp>201412011247169</z30-upd-time-stamp>\n" +
			    "        <z30-ip-last-return-v6/>\n" +
			    "      </z30>\n" +
			    "      <z13>\n" +
			    "        <translate-change-active-library>BOD50</translate-change-active-library>\n" +
			    "        <z13-doc-number>010276745</z13-doc-number>\n" +
			    "        <z13-year>1989</z13-year>\n" +
			    "        <z13-open-date>20110714</z13-open-date>\n" +
			    "        <z13-update-date>20140927</z13-update-date>\n" +
			    "        <z13-call-no-key/>\n" +
			    "        <z13-call-no-code>05000</z13-call-no-code>\n" +
			    "        <z13-call-no>DB2241.H38 A5 1989b</z13-call-no>\n" +
			    "        <z13-author-code>1001</z13-author-code>\n" +
			    "        <z13-author>Havel, Václav.</z13-author>\n" +
			    "        <z13-title-code/>\n" +
			    "        <z13-title>Living in truth : twenty-two essays published on the occasion of the award of the Erasmus Prize to V</z13-title>\n" +
			    "        <z13-imprint-code>260</z13-imprint-code>\n" +
			    "        <z13-imprint>London : Faber, 1989.</z13-imprint>\n" +
			    "        <z13-isbn-issn-code>020</z13-isbn-issn-code>\n" +
			    "        <z13-isbn-issn>0571144403</z13-isbn-issn>\n" +
			    "        <z13-upd-time-stamp>201312170902505</z13-upd-time-stamp>\n" +
			    "      </z13>\n" +
			    "      <status>Due date: 20/01/15</status>\n" +
			    "    </item>\n" +
			    "    <item href=\"http://aleph-dev.bodleian.ox.ac.uk:1891/rest-dlf/record/BIB01010276745/items/BOD50010276745000030\">\n" +
			    "      <z30-sub-library-code>SSLBL</z30-sub-library-code>\n" +
			    "      <z30-item-process-status-code>OR</z30-item-process-status-code>\n" +
			    "      <z30-item-status-code>01</z30-item-status-code>\n" +
			    "      <z30-collection-code/>\n" +
			    "      <queue/>\n" +
			    "      <z30>\n" +
			    "        <translate-change-active-library>BOD50</translate-change-active-library>\n" +
			    "        <z30-doc-number>010276745</z30-doc-number>\n" +
			    "        <z30-item-sequence>    3.0</z30-item-sequence>\n" +
			    "        <z30-barcode>A18001514500</z30-barcode>\n" +
			    "        <z30-sub-library>Social Science Library</z30-sub-library>\n" +
			    "        <z30-material>Book</z30-material>\n" +
			    "        <z30-item-status>Books</z30-item-status>\n" +
			    "        <z30-open-date>20140927</z30-open-date>\n" +
			    "        <z30-update-date>20140927</z30-update-date>\n" +
			    "        <z30-cataloger/>\n" +
			    "        <z30-date-last-return>00000000</z30-date-last-return>\n" +
			    "        <z30-hour-last-return>0000</z30-hour-last-return>\n" +
			    "        <z30-ip-last-return/>\n" +
			    "        <z30-no-loans>000</z30-no-loans>\n" +
			    "        <z30-alpha>L</z30-alpha>\n" +
			    "        <z30-collection/>\n" +
			    "        <z30-call-no-type/>\n" +
			    "        <z30-call-no/>\n" +
			    "        <z30-call-no-key/>\n" +
			    "        <z30-call-no-2-type/>\n" +
			    "        <z30-call-no-2/>\n" +
			    "        <z30-call-no-2-key/>\n" +
			    "        <z30-description/>\n" +
			    "        <z30-note-opac/>\n" +
			    "        <z30-note-circulation/>\n" +
			    "        <z30-note-internal/>\n" +
			    "        <z30-order-number>SSL15285</z30-order-number>\n" +
			    "        <z30-inventory-number/>\n" +
			    "        <z30-inventory-number-date>00000000</z30-inventory-number-date>\n" +
			    "        <z30-last-shelf-report-date>00000000</z30-last-shelf-report-date>\n" +
			    "        <z30-price/>\n" +
			    "        <z30-shelf-report-number/>\n" +
			    "        <z30-on-shelf-date>00000000</z30-on-shelf-date>\n" +
			    "        <z30-on-shelf-seq>000000</z30-on-shelf-seq>\n" +
			    "        <z30-doc-number-2>000000000</z30-doc-number-2>\n" +
			    "        <z30-schedule-sequence-2>00000</z30-schedule-sequence-2>\n" +
			    "        <z30-copy-sequence-2>00000</z30-copy-sequence-2>\n" +
			    "        <z30-vendor-code/>\n" +
			    "        <z30-invoice-number/>\n" +
			    "        <z30-line-number>00000</z30-line-number>\n" +
			    "        <z30-pages/>\n" +
			    "        <z30-issue-date>00000000</z30-issue-date>\n" +
			    "        <z30-expected-arrival-date>00000000</z30-expected-arrival-date>\n" +
			    "        <z30-arrival-date>00000000</z30-arrival-date>\n" +
			    "        <z30-item-statistic/>\n" +
			    "        <z30-item-process-status>On order</z30-item-process-status>\n" +
			    "        <z30-copy-id/>\n" +
			    "        <z30-hol-doc-number>012550397</z30-hol-doc-number>\n" +
			    "        <z30-temp-location>No</z30-temp-location>\n" +
			    "        <z30-enumeration-a/>\n" +
			    "        <z30-enumeration-b/>\n" +
			    "        <z30-enumeration-c/>\n" +
			    "        <z30-enumeration-d/>\n" +
			    "        <z30-enumeration-e/>\n" +
			    "        <z30-enumeration-f/>\n" +
			    "        <z30-enumeration-g/>\n" +
			    "        <z30-enumeration-h/>\n" +
			    "        <z30-chronological-i/>\n" +
			    "        <z30-chronological-j/>\n" +
			    "        <z30-chronological-k/>\n" +
			    "        <z30-chronological-l/>\n" +
			    "        <z30-chronological-m/>\n" +
			    "        <z30-supp-index-o/>\n" +
			    "        <z30-85x-type/>\n" +
			    "        <z30-depository-id/>\n" +
			    "        <z30-linking-number>000000000</z30-linking-number>\n" +
			    "        <z30-gap-indicator/>\n" +
			    "        <z30-maintenance-count>000</z30-maintenance-count>\n" +
			    "        <z30-process-status-date>20140927</z30-process-status-date>\n" +
			    "        <z30-upd-time-stamp>201409270534089</z30-upd-time-stamp>\n" +
			    "        <z30-ip-last-return-v6/>\n" +
			    "      </z30>\n" +
			    "      <z13>\n" +
			    "        <translate-change-active-library>BOD50</translate-change-active-library>\n" +
			    "        <z13-doc-number>010276745</z13-doc-number>\n" +
			    "        <z13-year>1989</z13-year>\n" +
			    "        <z13-open-date>20110714</z13-open-date>\n" +
			    "        <z13-update-date>20140927</z13-update-date>\n" +
			    "        <z13-call-no-key/>\n" +
			    "        <z13-call-no-code>05000</z13-call-no-code>\n" +
			    "        <z13-call-no>DB2241.H38 A5 1989b</z13-call-no>\n" +
			    "        <z13-author-code>1001</z13-author-code>\n" +
			    "        <z13-author>Havel, Václav.</z13-author>\n" +
			    "        <z13-title-code/>\n" +
			    "        <z13-title>Living in truth : twenty-two essays published on the occasion of the award of the Erasmus Prize to V</z13-title>\n" +
			    "        <z13-imprint-code>260</z13-imprint-code>\n" +
			    "        <z13-imprint>London : Faber, 1989.</z13-imprint>\n" +
			    "        <z13-isbn-issn-code>020</z13-isbn-issn-code>\n" +
			    "        <z13-isbn-issn>0571144403</z13-isbn-issn>\n" +
			    "        <z13-upd-time-stamp>201312170902505</z13-upd-time-stamp>\n" +
			    "      </z13>\n" +
			    "      <status>On order</status>\n" +
			    "    </item>\n" +
			    "    <item href=\"http://aleph-dev.bodleian.ox.ac.uk:1891/rest-dlf/record/BIB01010276745/items/STJ50010276745000010\">\n" +
			    "      <z30-sub-library-code>STJCL</z30-sub-library-code>\n" +
			    "      <z30-item-process-status-code/>\n" +
			    "      <z30-item-status-code>01</z30-item-status-code>\n" +
			    "      <z30-collection-code/>\n" +
			    "      <queue/>\n" +
			    "      <z30>\n" +
			    "        <translate-change-active-library>STJ50</translate-change-active-library>\n" +
			    "        <z30-doc-number>010276745</z30-doc-number>\n" +
			    "        <z30-item-sequence>    1.0</z30-item-sequence>\n" +
			    "        <z30-barcode>302430584</z30-barcode>\n" +
			    "        <z30-sub-library>St John's College Library</z30-sub-library>\n" +
			    "        <z30-material>Book</z30-material>\n" +
			    "        <z30-item-status>Books</z30-item-status>\n" +
			    "        <z30-open-date>19971107</z30-open-date>\n" +
			    "        <z30-update-date>19971107</z30-update-date>\n" +
			    "        <z30-cataloger>BrumfitP</z30-cataloger>\n" +
			    "        <z30-date-last-return>20031202</z30-date-last-return>\n" +
			    "        <z30-hour-last-return>0000</z30-hour-last-return>\n" +
			    "        <z30-ip-last-return/>\n" +
			    "        <z30-no-loans>001</z30-no-loans>\n" +
			    "        <z30-alpha>L</z30-alpha>\n" +
			    "        <z30-collection/>\n" +
			    "        <z30-call-no-type>8</z30-call-no-type>\n" +
			    "        <z30-call-no>POL / 645 / CZE / HAV</z30-call-no>\n" +
			    "        <z30-call-no-key>8 pol 645 cze hav</z30-call-no-key>\n" +
			    "        <z30-call-no-2-type/>\n" +
			    "        <z30-call-no-2/>\n" +
			    "        <z30-call-no-2-key/>\n" +
			    "        <z30-description/>\n" +
			    "        <z30-note-opac/>\n" +
			    "        <z30-note-circulation/>\n" +
			    "        <z30-note-internal/>\n" +
			    "        <z30-order-number/>\n" +
			    "        <z30-inventory-number/>\n" +
			    "        <z30-inventory-number-date>20130815</z30-inventory-number-date>\n" +
			    "        <z30-last-shelf-report-date>20140729</z30-last-shelf-report-date>\n" +
			    "        <z30-price/>\n" +
			    "        <z30-shelf-report-number>stj2014-000000026</z30-shelf-report-number>\n" +
			    "        <z30-on-shelf-date>20140813</z30-on-shelf-date>\n" +
			    "        <z30-on-shelf-seq>001633</z30-on-shelf-seq>\n" +
			    "        <z30-doc-number-2>000000000</z30-doc-number-2>\n" +
			    "        <z30-schedule-sequence-2>00000</z30-schedule-sequence-2>\n" +
			    "        <z30-copy-sequence-2>00000</z30-copy-sequence-2>\n" +
			    "        <z30-vendor-code/>\n" +
			    "        <z30-invoice-number/>\n" +
			    "        <z30-line-number>00000</z30-line-number>\n" +
			    "        <z30-pages/>\n" +
			    "        <z30-issue-date>00000000</z30-issue-date>\n" +
			    "        <z30-expected-arrival-date>00000000</z30-expected-arrival-date>\n" +
			    "        <z30-arrival-date>19971107</z30-arrival-date>\n" +
			    "        <z30-item-statistic/>\n" +
			    "        <z30-item-process-status>Not in Process</z30-item-process-status>\n" +
			    "        <z30-copy-id/>\n" +
			    "        <z30-hol-doc-number>000779515</z30-hol-doc-number>\n" +
			    "        <z30-temp-location>No</z30-temp-location>\n" +
			    "        <z30-enumeration-a/>\n" +
			    "        <z30-enumeration-b/>\n" +
			    "        <z30-enumeration-c/>\n" +
			    "        <z30-enumeration-d/>\n" +
			    "        <z30-enumeration-e/>\n" +
			    "        <z30-enumeration-f/>\n" +
			    "        <z30-enumeration-g/>\n" +
			    "        <z30-enumeration-h/>\n" +
			    "        <z30-chronological-i/>\n" +
			    "        <z30-chronological-j/>\n" +
			    "        <z30-chronological-k/>\n" +
			    "        <z30-chronological-l/>\n" +
			    "        <z30-chronological-m/>\n" +
			    "        <z30-supp-index-o/>\n" +
			    "        <z30-85x-type/>\n" +
			    "        <z30-depository-id/>\n" +
			    "        <z30-linking-number>000000000</z30-linking-number>\n" +
			    "        <z30-gap-indicator/>\n" +
			    "        <z30-maintenance-count>000</z30-maintenance-count>\n" +
			    "        <z30-process-status-date>00000000</z30-process-status-date>\n" +
			    "        <z30-upd-time-stamp>201408131601007</z30-upd-time-stamp>\n" +
			    "        <z30-ip-last-return-v6/>\n" +
			    "      </z30>\n" +
			    "      <z13>\n" +
			    "        <translate-change-active-library>STJ50</translate-change-active-library>\n" +
			    "        <z13-doc-number>010276745</z13-doc-number>\n" +
			    "        <z13-year>1989</z13-year>\n" +
			    "        <z13-open-date>20110714</z13-open-date>\n" +
			    "        <z13-update-date>20140927</z13-update-date>\n" +
			    "        <z13-call-no-key/>\n" +
			    "        <z13-call-no-code>05000</z13-call-no-code>\n" +
			    "        <z13-call-no>DB2241.H38 A5 1989b</z13-call-no>\n" +
			    "        <z13-author-code>1001</z13-author-code>\n" +
			    "        <z13-author>Havel, Václav.</z13-author>\n" +
			    "        <z13-title-code/>\n" +
			    "        <z13-title>Living in truth : twenty-two essays published on the occasion of the award of the Erasmus Prize to V</z13-title>\n" +
			    "        <z13-imprint-code>260</z13-imprint-code>\n" +
			    "        <z13-imprint>London : Faber, 1989.</z13-imprint>\n" +
			    "        <z13-isbn-issn-code>020</z13-isbn-issn-code>\n" +
			    "        <z13-isbn-issn>0571144403</z13-isbn-issn>\n" +
			    "        <z13-upd-time-stamp>201312170902505</z13-upd-time-stamp>\n" +
			    "      </z13>\n" +
			    "      <status>Available</status>\n" +
			    "    </item>\n" +
			    "  </items>\n" +
			    "</get-item-list>";


	private String errorXML = "<SEGMENTS xmlns=\"http://www.exlibrisgroup.com/xsd/jaguar/search\">"
		+"<JAGROOT>"
		+"<RESULT>"
		+"<ERROR MESSAGE=\"PrimoGetItWS Remote Search Key is missing or expired\" CODE=\"-6\"/>"
		+"</RESULT>"
		+"</JAGROOT>"
		+"</SEGMENTS>";



	protected void setUp() throws Exception {
		super.setUp();
		service = new AlephService(WEBRESOURCE_URL);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFilterOLISResponse() {

		try {
			Collection<SearObject> beans =
				AlephService.filterResponse(nameSpaceURI, OLIS_XML);
			Assert.assertEquals(9, beans.size());

		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
	}

	public void testParser() {

		String originalString = "<tag>This string is mine & yours <trouble &amp; strife></tag>";

		Pattern pattern = Pattern.compile("&(?!(amp|apos|quot|lt|gt);)");
		Matcher mat = pattern.matcher(originalString);
		String result = mat.replaceAll("&amp;");
		System.out.println("parser ["+result+"]");
		Assert.assertEquals("<tag>This string is mine &amp; yours <trouble &amp; strife></tag>", result);
	}

	public void testFilterErrorResponse() {

		try {
			AlephService.filterResponse(nameSpaceURI, errorXML);

			//Assert.fail("Exception expected");

		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
	}

	public void testORAoJSON() throws Exception {
		String id = "ORAdebe641a-17ca-4196-ab2c-fe7565ced721";
		ResponseBean responseBean = new ResponseBean(id);
		Collection<SearObject> beans = AlephService.filterResponse(nameSpaceURI, OLIS_XML);
		responseBean.addSearObjects(beans);

		JSONObject json = responseBean.toJSON("2009-06-09T15:39:52.831+02:00");
		// Check the basics
		assertEquals("0.5", json.getString("version"));
		assertNotNull(json.get("institution"));
		assertEquals("http://www.ox.ac.uk", json.getJSONObject("institution").getString("href"));
		// Check the document
		JSONArray document = json.getJSONArray("document");
		assertNotNull(document);
		JSONObject first = document.getJSONObject(0);
		assertNotNull(first);
		assertEquals("ORAdebe641a-17ca-4196-ab2c-fe7565ced721", first.getString("id"));
		assertNotNull(first.get("item"));
		assertEquals("Balliol College Library"
				,first.getJSONArray("item").getJSONObject(0).getString("libname"));
	}

}

/**
 * Copyright 2012 Miroslav Šulc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.fordfrog.ruian2pgsql.convertors;

import com.fordfrog.ruian2pgsql.Config;
import com.fordfrog.ruian2pgsql.containers.StavebniObjekt;
import com.fordfrog.ruian2pgsql.utils.Namespaces;
import com.fordfrog.ruian2pgsql.utils.PreparedStatementEx;
import com.fordfrog.ruian2pgsql.utils.Utils;
import com.fordfrog.ruian2pgsql.utils.XMLUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Convertor for StavebniObjekt element.
 *
 * @author fordfrog
 */
public class StavebniObjektConvertor
        extends AbstractSaveConvertor<StavebniObjekt> {

    /**
     * Namespace of the element.
     */
    private static final String NAMESPACE = Namespaces.STAV_OBJ_INT_TYPY;
    /**
     * SQL statement for checking whether the item already exists.
     */
    private static final String SQL_EXISTS =
            "SELECT 1 FROM rn_stavebni_objekt WHERE kod = ?";
    /**
     * SQL statement for insertion of new item.
     */
    private static final String SQL_INSERT = "INSERT INTO rn_stavebni_objekt "
            + "(nespravny, identifikacni_parcela_id, momc_kod, cobce_kod, "
            + "budova_id, cisla_domovni, dokonceni, je_vytah_kod, "
            + "zmena_grafiky, druh_konstrukce_kod, zmena_detailu, "
            + "obestaveny_prostor, pocet_bytu, pocet_podlazi, "
            + "podlahova_plocha, pripoj_el_energie, pripoj_kanal_sit_kod, "
            + "pripoj_plyn_kod, pripoj_vodovod_kod, typ_kod, zastavena_plocha, "
            + "zpusob_vytapeni_kod, zpusob_vyuziti_kod, id_trans_ruian, "
            + "plati_od, nz_id_globalni, definicni_bod, hranice, kod) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
            + "?, ?, ?, ?, ?, ?, ?, %FUNCTION%(?), %FUNCTION%(?), ?)";
    /**
     * SQL statement for update of existing item.
     */
    private static final String SQL_UPDATE = "UPDATE rn_stavebni_objekt "
            + "SET nespravny = ?, identifikacni_parcela_id = ?, momc_kod = ?, "
            + "cobce_kod = ?, budova_id = ?, cisla_domovni = ?, dokonceni = ?, "
            + "je_vytah_kod = ?, zmena_grafiky = ?, druh_konstrukce_kod = ?, "
            + "zmena_detailu = ?, obestaveny_prostor = ?, pocet_bytu = ?, "
            + "pocet_podlazi = ?, podlahova_plocha = ?, pripoj_el_energie = ?, "
            + "pripoj_kanal_sit_kod = ?, pripoj_plyn_kod = ?, "
            + "pripoj_vodovod_kod = ?, typ_kod = ?, zastavena_plocha = ?, "
            + "zpusob_vytapeni_kod = ?, zpusob_vyuziti_kod = ?, "
            + "id_trans_ruian = ?, plati_od = ?, nz_id_globalni = ?, "
            + "definicni_bod = %FUNCTION%(?), hranice = %FUNCTION%(?), "
            + "item_timestamp = timezone('utc', now()), deleted = false "
            + "WHERE kod = ? AND id_trans_ruian <= ?";
    /**
     * SQL statement for insertion of new item.
     */
    private static final String SQL_INSERT_NO_GIS =
            "INSERT INTO rn_stavebni_objekt "
            + "(nespravny, identifikacni_parcela_id, momc_kod, cobce_kod, "
            + "budova_id, cisla_domovni, dokonceni, je_vytah_kod, "
            + "zmena_grafiky, druh_konstrukce_kod, zmena_detailu, "
            + "obestaveny_prostor, pocet_bytu, pocet_podlazi, "
            + "podlahova_plocha, pripoj_el_energie, pripoj_kanal_sit_kod, "
            + "pripoj_plyn_kod, pripoj_vodovod_kod, typ_kod, zastavena_plocha, "
            + "zpusob_vytapeni_kod, zpusob_vyuziti_kod, id_trans_ruian, "
            + "plati_od, nz_id_globalni, kod) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
            + "?, ?, ?, ?, ?, ?, ?, ?)";
    /**
     * SQL statement for update of existing item.
     */
    private static final String SQL_UPDATE_NO_GIS =
            "UPDATE rn_stavebni_objekt "
            + "SET nespravny = ?, identifikacni_parcela_id = ?, momc_kod = ?, "
            + "cobce_kod = ?, budova_id = ?, cisla_domovni = ?, dokonceni = ?, "
            + "je_vytah_kod = ?, zmena_grafiky = ?, druh_konstrukce_kod = ?, "
            + "zmena_detailu = ?, obestaveny_prostor = ?, pocet_bytu = ?, "
            + "pocet_podlazi = ?, podlahova_plocha = ?, pripoj_el_energie = ?, "
            + "pripoj_kanal_sit_kod = ?, pripoj_plyn_kod = ?, "
            + "pripoj_vodovod_kod = ?, typ_kod = ?, zastavena_plocha = ?, "
            + "zpusob_vytapeni_kod = ?, zpusob_vyuziti_kod = ?, "
            + "id_trans_ruian = ?, plati_od = ?, nz_id_globalni = ?, "
            + "item_timestamp = timezone('utc', now()), deleted = false "
            + "WHERE kod = ? AND id_trans_ruian <= ?";
    /**
     * SQL statement for deleting of DetainiTEA.
     */
    private static final String SQL_DELETE_DETAILNI_TEA =
            "DELETE FROM rn_detailni_tea WHERE stavobj_kod = ?";
    /**
     * SQL statement for deleting of ZpusobyOchranyObjektu.
     */
    private static final String SQL_DELETE_ZPUSOBY_OCHRANY_OBJEKTU =
            "DELETE FROM rn_zpusob_ochrany_objektu WHERE stavobj_kod = ?";
    /**
     * Prepared statement for deleting of DetainiTEA.
     */
    private final PreparedStatement pstmDeleteDetailniTEA;
    /**
     * Prepared statement for deleting of ZpusobyOchranyObjektu.
     */
    private final PreparedStatement pstmDeleteZpusobyOchranyObjektu;
    /**
     * Convertor for DetailniTEA.
     */
    private final Convertor convertorDetailniTEA;
    /**
     * Convertor for ZpusobyOchranyObjektu.
     */
    private final Convertor convertorZpusobyOchranyObjektu;
    /**
     * Convertor for DetailniTEA.
     */
    private final DetailniTEAConvertor detailniTEAConvertor;
    /**
     * Convertor for ZpusobOchranyObjektu.
     */
    private final ZpusobOchranyObjektuConvertor zpusobOchranyObjektuConvertor;

    /**
     * Creates new instance of StavebniObjektConvertor.
     *
     * @param con database connection
     *
     * @throws SQLException Thrown if problem occurred while communicating with
     *                      database.
     */
    public StavebniObjektConvertor(final Connection con) throws SQLException {
        super(StavebniObjekt.class, Namespaces.VYMENNY_FORMAT_TYPY,
                "StavebniObjekt", con, SQL_EXISTS, SQL_INSERT, SQL_UPDATE,
                SQL_INSERT_NO_GIS, SQL_UPDATE_NO_GIS);

        pstmDeleteDetailniTEA = con.prepareStatement(SQL_DELETE_DETAILNI_TEA);
        pstmDeleteZpusobyOchranyObjektu =
                con.prepareStatement(SQL_DELETE_ZPUSOBY_OCHRANY_OBJEKTU);

        detailniTEAConvertor = new DetailniTEAConvertor(con);
        zpusobOchranyObjektuConvertor = new ZpusobOchranyObjektuConvertor(con);

        convertorDetailniTEA = new CollectionConvertor(
                Namespaces.STAV_OBJ_INT_TYPY, "DetailniTEA",
                Namespaces.STAV_OBJ_INT_TYPY, "DetailniTEA",
                detailniTEAConvertor);
        convertorZpusobyOchranyObjektu = new CollectionConvertor(
                Namespaces.STAV_OBJ_INT_TYPY, "ZpusobyOchrany",
                Namespaces.COMMON_TYPY, "ZpusobOchrany",
                zpusobOchranyObjektuConvertor);
    }

    @Override
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    protected void fill(final PreparedStatement pstm, final StavebniObjekt item,
            final boolean update) throws SQLException {
        final PreparedStatementEx pstmEx = new PreparedStatementEx(pstm);
        int index = 1;
        pstmEx.setBoolean(index++, item.getNespravny());
        pstmEx.setLong(index++, item.getIdentifikacniParcelaId());
        pstmEx.setInt(index++, item.getMomcKod());
        pstmEx.setInt(index++, item.getCobceKod());
        pstmEx.setLong(index++, item.getBudovaId());

        if (Config.isMysqlDriver()) {
            pstm.setString(index++, Arrays.toString(item.getCislaDomovni()));
        } else {
            pstmEx.setIntArray(index++, item.getCislaDomovni());
        }

        pstmEx.setDate(index++, item.getDokonceni());
        pstmEx.setInt(index++, item.getJeVytahKod());
        pstmEx.setBoolean(index++, item.getZmenaGrafiky());
        pstmEx.setInt(index++, item.getDruhKonstrukceKod());
        pstmEx.setBoolean(index++, item.getZmenaDetailu());
        pstmEx.setInt(index++, item.getObestavenyProstor());
        pstmEx.setInt(index++, item.getPocetBytu());
        pstmEx.setInt(index++, item.getPocetPodlazi());
        pstmEx.setInt(index++, item.getPodlahovaPlocha());
        pstmEx.setBoolean(index++, item.getPripojElEnergie());
        pstmEx.setInt(index++, item.getPripojKanalSitKod());
        pstmEx.setInt(index++, item.getPripojPlynKod());
        pstmEx.setInt(index++, item.getPripojVodovodKod());
        pstmEx.setInt(index++, item.getTypKod());
        pstmEx.setInt(index++, item.getZastavenaPlocha());
        pstmEx.setInt(index++, item.getZpusobVytapeniKod());
        pstmEx.setInt(index++, item.getZpusobVyuzitiKod());
        pstm.setLong(index++, item.getIdTransRuian());
        pstmEx.setDate(index++, item.getPlatiOd());
        pstm.setLong(index++, item.getNzIdGlobalni());

        if (!Config.isNoGis()) {
            pstm.setString(index++, item.getDefinicniBod());
            pstm.setString(index++, item.getHranice());
        }

        pstm.setInt(index++, item.getKod());

        if (update) {
            pstm.setLong(index++, item.getIdTransRuian());
        }
    }

    @Override
    protected void fillExists(final PreparedStatement pstm,
            final StavebniObjekt item) throws SQLException {
        pstm.setInt(1, item.getKod());
    }

    @Override
    protected void processElement(final XMLStreamReader reader,
            final StavebniObjekt item) throws XMLStreamException, SQLException {
        switch (reader.getNamespaceURI()) {
            case NAMESPACE:
                switch (reader.getLocalName()) {
                    case "CastObce":
                        item.setCobceKod(
                                Utils.getCastObceKod(reader, NAMESPACE));
                        break;
                    case "CislaDomovni":
                        processCislaDomovni(reader, item);
                        break;
                    case "DetailniTEA":
                        detailniTEAConvertor.setStavebniObjektId(item.getKod());
                        convertorDetailniTEA.convert(reader);
                        break;
                    case "Dokonceni":
                        item.setDokonceni(
                                Utils.parseTimestamp(reader.getElementText()));
                        break;
                    case "DruhKonstrukceKod":
                        item.setDruhKonstrukceKod(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "Geometrie":
                        Utils.processGeometrie(
                                reader, getConnection(), item, NAMESPACE);
                        break;
                    case "GlobalniIdNavrhuZmeny":
                        item.setNzIdGlobalni(
                                Long.parseLong(reader.getElementText()));
                        break;
                    case "IdentifikacniParcela":
                        item.setIdentifikacniParcelaId(
                                Utils.getIdentifikacniParcelaId(
                                reader, NAMESPACE));
                        break;
                    case "IdTransakce":
                        item.setIdTransRuian(
                                Long.parseLong(reader.getElementText()));
                        break;
                    case "IsknBudovaId":
                        item.setBudovaId(
                                Long.parseLong(reader.getElementText()));
                        break;
                    case "Kod":
                        item.setKod(
                                Integer.parseInt(reader.getElementText()));
                        deleteDetailniTEA(item.getKod());
                        deleteZpusobyOchranyObjektu(item.getKod());
                        break;
                    case "Momc":
                        item.setMomcKod(Utils.getMomcKod(reader, NAMESPACE));
                        break;
                    case "Nespravny":
                        item.setNespravny(
                                Boolean.valueOf(reader.getElementText()));
                        break;
                    case "ObestavenyProstor":
                        item.setObestavenyProstor(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "PlatiOd":
                        item.setPlatiOd(
                                Utils.parseTimestamp(reader.getElementText()));
                        break;
                    case "PocetBytu":
                        item.setPocetBytu(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "PocetPodlazi":
                        item.setPocetPodlazi(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "PodlahovaPlocha":
                        item.setPodlahovaPlocha(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "PripojeniKanalizaceKod":
                        item.setPripojKanalSitKod(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "PripojeniPlynKod":
                        item.setPripojPlynKod(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "PripojeniVodovodKod":
                        item.setPripojVodovodKod(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "TypStavebnihoObjektuKod":
                        item.setTypKod(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "VybaveniVytahemKod":
                        item.setJeVytahKod(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "ZastavenaPlocha":
                        item.setZastavenaPlocha(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "ZpusobVytapeniKod":
                        item.setZpusobVytapeniKod(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "ZpusobVyuzitiKod":
                        item.setZpusobVyuzitiKod(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "ZpusobyOchrany":
                        zpusobOchranyObjektuConvertor.setStavebniObjektId(
                                item.getKod());
                        convertorZpusobyOchranyObjektu.convert(reader);
                        break;
                    default:
                        XMLUtils.processUnsupported(reader);
                }

                break;
            default:
                XMLUtils.processUnsupported(reader);
        }
    }

    /**
     * Processes CislaDomovni element.
     *
     * @param reader XML stream reader
     * @param item   item
     *
     * @throws XMLStreamException Thrown if problem occurred while reading XML
     *                            stream.
     */
    private void processCislaDomovni(final XMLStreamReader reader,
            final StavebniObjekt item) throws XMLStreamException {
        while (reader.hasNext()) {
            final int event = reader.next();

            switch (event) {
                case XMLStreamReader.START_ELEMENT:
                    processCislaDomovniElement(reader, item);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (XMLUtils.isSameElement(
                            NAMESPACE, "CislaDomovni", reader)) {
                        return;
                    }
            }
        }
    }

    /**
     * Processes sub-elements of CislaDomovni element.
     *
     * @param reader XML stream reader
     * @param item   item
     *
     * @throws XMLStreamException Thrown if problem occurred while reading XML
     *                            stream.
     */
    private void processCislaDomovniElement(final XMLStreamReader reader,
            final StavebniObjekt item) throws XMLStreamException {
        switch (reader.getNamespaceURI()) {
            case Namespaces.COMMON_TYPY:
                switch (reader.getLocalName()) {
                    case "CisloDomovni":
                        item.addCisloDomovni(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    default:
                        XMLUtils.processUnsupported(reader);
                }
                break;
            default:
                XMLUtils.processUnsupported(reader);
        }
    }

    /**
     * Deletes DetailniTEA that belong to this StavebniObjekt.
     *
     * @param stavebniObjektId StavebniObjekt id
     *
     * @throws SQLException Thrown if problem occurred while deleting the items.
     */
    private void deleteDetailniTEA(final Integer stavebniObjektId)
            throws SQLException {
        if (Config.isDryRun()) {
            return;
        }

        pstmDeleteDetailniTEA.clearParameters();
        pstmDeleteDetailniTEA.setInt(1, stavebniObjektId);
        pstmDeleteDetailniTEA.execute();
    }

    /**
     * Deletes ZpusobyOchranyObjektu that belong to this StavebniObjekt.
     *
     * @param stavebniObjektId StavebniObjekt id
     *
     * @throws SQLException Thrown if problem occurred while deleting the items.
     */
    private void deleteZpusobyOchranyObjektu(final Integer stavebniObjektId)
            throws SQLException {
        if (Config.isDryRun()) {
            return;
        }

        pstmDeleteZpusobyOchranyObjektu.clearParameters();
        pstmDeleteZpusobyOchranyObjektu.setInt(1, stavebniObjektId);
        pstmDeleteZpusobyOchranyObjektu.execute();
    }
}

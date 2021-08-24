package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.bh.utils.duplicarRegistro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.util.Collection;

public class inserirPedido {

    public static  BigDecimal inserePedido(DynamicVO tbhCabVO, BigDecimal ordemCarga) throws Exception {
        JapeWrapper cabDAO = JapeFactory.dao("CabecalhoNota");
        JapeWrapper iteDAO = JapeFactory.dao("ItemNota");
        JapeWrapper parDAO = JapeFactory.dao("Parceiro");
        JapeWrapper tbhIteDAO = JapeFactory.dao("AD_TBHITE");
        JapeWrapper voaDAO = JapeFactory.dao("VolumeAlternativo");
        JapeWrapper cplDAO = JapeFactory.dao("ComplementoParc");

        ImpostosHelpper impHelper = new ImpostosHelpper();

        DynamicVO parVO = parDAO.findOne("CODPARC = ?", tbhCabVO.asBigDecimal("CODPARC"));

        BigDecimal codTipVenda = tbhCabVO.asBigDecimalOrZero("CODTIPVENDA");
        if (codTipVenda.compareTo(BigDecimal.ZERO) == 0) {
            DynamicVO cplVO = cplDAO.findOne("CODPARC = ?", tbhCabVO.asBigDecimal("CODPARC"));
            codTipVenda = cplVO.asBigDecimalOrZero("SUGTIPNEGSAID");
        }

        /*Inserir Nota*/
        String observacao = " ";
        if (null!=tbhCabVO.asString("OBSERVACAO")){
            observacao = tbhCabVO.asString("OBSERVACAO");
        }
        if (null!=tbhCabVO.asString("NUMPEDCLI")){
            observacao = observacao.concat("No. Pedido: "+tbhCabVO.asString("NUMPEDCLI"));
        }

        DynamicVO cabVO = cabDAO.create()
                .set("CODPARC", tbhCabVO.asBigDecimal("CODPARC"))
                .set("CODEMP", tbhCabVO.asBigDecimal("CODEMP"))
                .set("CODEMPNEGOC", tbhCabVO.asBigDecimal("CODEMP"))
                .set("CODTIPVENDA", codTipVenda)
                .set("DHTIPVENDA", duplicarRegistro.getDataMaxTipoNeg(codTipVenda))
                .set("CODTIPOPER", tbhCabVO.asBigDecimal("CODTIPOPER"))
                .set("DHTIPOPER", duplicarRegistro.getDataMaxOper(tbhCabVO.asBigDecimal("CODTIPOPER")))
                .set("DTNEG", tbhCabVO.asTimestamp("DTPREV"))
                .set("DTALTER", TimeUtils.getNow())
                .set("DTMOV", tbhCabVO.asTimestamp("DTPREV"))
                .set("DTFATUR", tbhCabVO.asTimestamp("DTPREV"))
                .set("CODNAT", BigDecimal.valueOf(1010100))
                .set("CODCENCUS", BigDecimal.valueOf(20000))
                .set("NUMNOTA", BigDecimal.ZERO)
                .set("DTENTSAI", tbhCabVO.asTimestamp("DTPREV"))
                .set("ORDEMCARGA", ordemCarga)
                .set("SEQCARGA", tbhCabVO.asBigDecimal("SEQCARGA"))
                .set("OBSERVACAO", observacao)
                .set("CODVEND", parVO.asBigDecimalOrZero("CODVEND"))
                .set("AD_RECEBECX", tbhCabVO.asString("RECEBECX"))
                .set("TIPFRETE", "N")
                .set("CIF_FOB", tbhCabVO.asString("CIF_FOB"))
                .set("CODOBSPADRAO", tbhCabVO.asBigDecimal("CODOBSPADRAO"))
                .set("TIPMOV", "P")
                .save();

        /*Inserir Itens*/
        Collection<DynamicVO> itens = tbhIteDAO.find("NUNOTAPR = ? AND NVL(QTDNEG,0) > 0"
                , tbhCabVO.asBigDecimal("NUNOTAPR"));
        for (DynamicVO item : itens) {

            BigDecimal qtdNeg = item.asBigDecimal("QTDNEG");

            /*VERIFICA SE COVOL É DIFERENTE DO PADRÃO*/
            String codVol = "PT";
            if ("S".equals(tbhCabVO.asString("RECEBECX"))) {
                codVol = "CX";
            }

            if (!codVol.equals(item.asString("CODVOL"))) {
                DynamicVO voaVO = voaDAO.findOne("CODVOL = ? AND CODPROD = ?", item.asString("CODVOL"),item.asBigDecimal("CODPROD"));
                if (voaVO!=null) {
                    if ("D".equals(voaVO.asString("DIVIDEMULTIPLICA"))) {
                        qtdNeg = item.asBigDecimal("QTDNEG").divide(voaVO.asBigDecimal("QUANTIDADE"), 4, RoundingMode.HALF_UP);
                    } else {
                        qtdNeg = item.asBigDecimal("QTDNEG").multiply(voaVO.asBigDecimal("QUANTIDADE")).setScale(4, RoundingMode.HALF_UP);
                    }
                }
                if (voaVO==null){
                    codVol = item.asString("Produto.CODVOL");
                }
            }
            String controle = " ";

            if ("S".equals(item.asString("Produto.TIPCONTEST"))) {
                controle = "SACOLA";
                if ("S".equals(parVO.asString("AD_RECEBECX"))) {
                    controle = "CAIXA";
                }
            }

            BigDecimal precoTab = impHelper.buscaPrecoTabelaAtual(parVO.asBigDecimal("CODTAB")
                    , item.asBigDecimal("CODPROD"));

            BigDecimal codLocal = item.asBigDecimalOrZero("Produto.CODLOCALPADRAO");
            if (codLocal.compareTo(BigDecimal.ZERO) == 0) {
                codLocal = BigDecimal.valueOf(105);
            }
            if ("S".equals(item.asString("Produto.USOPROD"))){
                codLocal = BigDecimal.ZERO;
            }
            iteDAO.create()
                    .set("NUNOTA", cabVO.asBigDecimal("NUNOTA"))
                    .set("CODPROD", item.asBigDecimal("CODPROD"))
                    .set("QTDNEG", item.asBigDecimal("QTDNEG"))
                    .set("VLRUNIT", precoTab)
                    .set("VLRTOT", precoTab.multiply(qtdNeg))
                    .set("CODVOL", codVol)
                    .set("QTDNEG", qtdNeg)
                    .set("CONTROLE", controle)
                    .set("RESERVA", "N")
                    .set("USOPROD", item.asString("Produto.USOPROD"))
                    .set("CODLOCALORIG", codLocal)
                    .set("ATUALESTOQUE", BigDecimal.ZERO)
                    .save();
        }

        EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwfEntityFacade.getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);
        sql.appendSql(" SELECT SUM(CASE WHEN VOA.DIVIDEMULTIPLICA = 'M' THEN ITE.QTDNEG/VOA.QUANTIDADE "+
                " WHEN VOA.DIVIDEMULTIPLICA = 'D' THEN ITE.QTDNEG*VOA.QUANTIDADE "+
                " ELSE ITE.QTDNEG END) QTDVOL FROM TGFITE ITE "+
                " LEFT JOIN TGFVOA VOA ON ITE.CODVOL = VOA.CODVOL AND ITE.CODPROD = VOA.CODPROD "+
                " WHERE ITE.NUNOTA = :NUNOTA "+
                " GROUP BY ITE.NUNOTA ");
        sql.setNamedParameter("NUNOTA",cabVO.asBigDecimal("NUNOTA"));
        ResultSet resultSet = sql.executeQuery();

        while (resultSet.next()) {
            cabDAO.prepareToUpdate(cabVO)
                    .set("QTDVOL",resultSet.getBigDecimal("QTDVOL"))
                    .update();
        }

        impHelper.totalizarNota(cabVO.asBigDecimal("NUNOTA"));
        impHelper.salvarNota();
        impHelper.setForcarRecalculo(true);
        impHelper.calcularImpostos(cabVO.asBigDecimal("NUNOTA"));
        BarramentoRegra bRegras = BarramentoRegra.build(CACHelper.class, "regrasConfirmacaoCAC.xml", AuthenticationInfo.getCurrent());
        ConfirmacaoNotaHelper.confirmarNota(cabVO.asBigDecimal("NUNOTA"), bRegras, true);

        return cabVO.asBigDecimal("NUNOTA");

    }
}

/*
NATUREZA - ok
CENTRO DE RESULTADO - ok
VENDEDOR - PARCEIRO - OK
CALCULAR IMPOSTO - OK
STATUS DO PEDIDO, AGUARDANDO LIBERAÇÃO
 */

package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import com.sankhya.util.TimeUtils;
import br.com.sankhya.bh.utils.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Collection;

public class acaoGerarPedidos implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        JapeWrapper cabDAO = JapeFactory.dao("CabecalhoNota");
        JapeWrapper iteDAO = JapeFactory.dao("ItemNota");
        JapeWrapper ordDAO = JapeFactory.dao("OrdemCarga");
        JapeWrapper parDAO = JapeFactory.dao("Parceiro");
        JapeWrapper tbhIteDAO = JapeFactory.dao("AD_TBHITE");
        JapeWrapper tbhCabDAO = JapeFactory.dao("AD_TBHCAB");
        JapeWrapper voaDAO = JapeFactory.dao("VolumeAlternativo");
        JapeWrapper cplDAO = JapeFactory.dao("ComplementoParc");

        BarramentoRegra bRegras = BarramentoRegra.build(CACHelper.class, "regrasConfirmacaoCAC.xml", AuthenticationInfo.getCurrent());
        ImpostosHelpper impHelper = new ImpostosHelpper();

        Registro[] registros = contextoAcao.getLinhas();
        for (Registro registro : registros) {
            if ("F".equals(registro.getCampo("STATUS"))) {

                DynamicVO parVO = parDAO.findOne("CODPARC = ?", registro.getCampo("CODPARC"));
                DynamicVO tbhCabVO = tbhCabDAO.findOne("NUNOTAPR = ?", registro.getCampo("NUNOTAPR"));

                DynamicVO tbhCabOrdensVO = tbhCabDAO.findOne("PREORDEM = ? AND DTPREV = ? AND ORDEMCARGA IS NOT NULL"
                        , tbhCabVO.asBigDecimal("PREORDEM")
                        , tbhCabVO.asTimestamp("DTPREV"));

                /*Inserir Ordem de Carga*/
                BigDecimal ordemCarga;

                if (null == tbhCabOrdensVO) {
                    DynamicVO orcVO = ordDAO.create()
                            .set("CODEMP", tbhCabVO.asBigDecimal("CODEMP"))
                            .set("DTINIC", tbhCabVO.asTimestamp("DTPREV"))
                            .set("DTPREVSAIDA", tbhCabVO.asTimestamp("DTPREV"))
                            .set("CODPARCTRANSP", BigDecimal.ONE)
                            .set("CODVEICULO", BigDecimal.ZERO)
                            .set("CODVEICULO", BigDecimal.ZERO)
                            .set("CODREG", tbhCabVO.asBigDecimalOrZero("CODREG"))
                            .set("SITUACAO", "A")
                            .set("TIPCARGA", "R")
                            .set("CODUSU", contextoAcao.getUsuarioLogado())
                            .set("DTALTER", TimeUtils.getNow())
                            .save();
                    ordemCarga = orcVO.asBigDecimal("ORDEMCARGA");
                } else {
                    ordemCarga = tbhCabOrdensVO.asBigDecimal("ORDEMCARGA");
                }

                BigDecimal codTipVenda = tbhCabVO.asBigDecimalOrZero("CODTIPVENDA");
                if (codTipVenda.compareTo(BigDecimal.ZERO) == 0) {
                    DynamicVO cplVO = cplDAO.findOne("CODPARC = ?", tbhCabVO.asBigDecimal("CODPARC"));
                    codTipVenda = cplVO.asBigDecimalOrZero("SUGTIPNEGSAID");
                }

                //ErroUtils.disparaErro(duplicarRegistro.getDataMaxTipoNeg(codTipVenda).toString());
                /*Inserir Nota*/
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
                        .set("OBSERVACAO", tbhCabVO.asString("OBSERVACAO"))
                        .set("CODVEND", parVO.asBigDecimalOrZero("CODVEND"))
                        .set("TIPMOV", "P")
                        .save();

                /*Inserir Itens*/
                Collection<DynamicVO> itens = tbhIteDAO.find("NUNOTAPR = ? AND NVL(QTDNEG,0) > 0"
                        , tbhCabVO.asBigDecimal("NUNOTAPR"));
                for (DynamicVO item : itens) {

                    BigDecimal qtdNeg = item.asBigDecimal("QTDNEG");

                    /*VERIFICA SE COVOL É DIFERENTE DO PADRÃO*/
                    String codVol = "PT";
                    if ("S".equals(parVO.asString("AD_RECEBECX"))) {
                        codVol = "CX";
                    }

                    if (!codVol.equals(item.asString("CODVOL"))) {
                        DynamicVO voaVO = voaDAO.findOne("CODVOL = ? AND CODPROD = ?", item.asString("CODVOL"),item.asBigDecimal("CODPROD"));
                        if ("D".equals(voaVO.asString("DIVIDEMULTIPLICA"))) {
                            qtdNeg = item.asBigDecimal("QTDNEG").divide(voaVO.asBigDecimal("QUANTIDADE"), 4, RoundingMode.HALF_UP);
                        } else {
                            qtdNeg = item.asBigDecimal("QTDNEG").multiply(voaVO.asBigDecimal("QUANTIDADE")).setScale(4, RoundingMode.HALF_UP);
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

                tbhCabDAO.prepareToUpdate(tbhCabVO)
                        .set("NUNOTA", cabVO.asBigDecimal("NUNOTA"))
                        .set("STATUS", "PG")
                        .set("ORDEMCARGA",ordemCarga)
                        .update();

                impHelper.totalizarNota(cabVO.asBigDecimal("NUNOTA"));
                impHelper.salvarNota();
                impHelper.setForcarRecalculo(true);
                impHelper.calcularImpostos(cabVO.asBigDecimal("NUNOTA"));
                ConfirmacaoNotaHelper.confirmarNota(cabVO.asBigDecimal("NUNOTA"), bRegras, true);

                contextoAcao.setMensagemRetorno("Pedido(s) de Venda gerado(s) para Pedido(s) Rapido 'Fechado'");
            }
        }
    }
}

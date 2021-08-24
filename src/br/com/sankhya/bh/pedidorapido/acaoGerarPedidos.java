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
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class acaoGerarPedidos implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        JapeWrapper ordDAO = JapeFactory.dao("OrdemCarga");
        JapeWrapper parDAO = JapeFactory.dao("Parceiro");
        JapeWrapper tbhCabDAO = JapeFactory.dao("AD_TBHCAB");

        Registro[] registros = contextoAcao.getLinhas();
        String obsMotorista = "";
        for (Registro registro : registros) {
            if ("F".equals(registro.getCampo("STATUS"))) {

                DynamicVO parVO = parDAO.findOne("CODPARC = ?", registro.getCampo("CODPARC"));
                DynamicVO tbhCabVO = tbhCabDAO.findOne("NUNOTAPR = ?", registro.getCampo("NUNOTAPR"));

                DynamicVO tbhCabOrdensVO = tbhCabDAO.findOne("PREORDEM = ? AND DTPREV = ? AND ORDEMCARGA IS NOT NULL"
                        , tbhCabVO.asBigDecimal("PREORDEM")
                        , tbhCabVO.asTimestamp("DTPREV"));

                /*Inserir Ordem de Carga*/
                BigDecimal ordemCarga;

                if(null != tbhCabVO.asString("OBSERVACAO")){
                    obsMotorista = obsMotorista+parVO.asString("NOMEPARC")+": "+tbhCabVO.asString("OBSERVACAO")+"\n";
                }

                DynamicVO orcVO;
                if (null == tbhCabOrdensVO) {
                    orcVO = ordDAO.create()
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
                            .set("ROTEIRO",obsMotorista)
                            .save();
                    ordemCarga = orcVO.asBigDecimal("ORDEMCARGA");
                } else {
                    ordemCarga = tbhCabOrdensVO.asBigDecimal("ORDEMCARGA");
                    ordDAO.prepareToUpdateByPK(tbhCabVO.asBigDecimal("CODEMP"),ordemCarga)
                            .set("ROTEIRO",obsMotorista)
                            .update();
                }
                BigDecimal nuNota = inserirPedido.inserePedido(tbhCabVO,ordemCarga);

                tbhCabDAO.prepareToUpdate(tbhCabVO)
                        .set("NUNOTA", nuNota)
                        .set("STATUS", "PG")
                        .set("ORDEMCARGA", ordemCarga)
                        .update();
            }
        }
        contextoAcao.setMensagemRetorno("Pedido(s) de Venda gerado(s) para Pedido(s) Rapido 'Fechado'");
    }
}

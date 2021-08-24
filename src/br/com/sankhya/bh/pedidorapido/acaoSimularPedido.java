package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;

public class acaoSimularPedido implements AcaoRotinaJava {
    JapeWrapper tbhCabDAO = JapeFactory.dao("AD_TBHCAB");
    JapeWrapper cabDAO = JapeFactory.dao("CabecalhoNota");

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] registros = contextoAcao.getLinhas();
        for (Registro registro : registros) {
            DynamicVO tbhCabVO = tbhCabDAO.findOne("NUNOTAPR = ?", registro.getCampo("NUNOTAPR"));
            BigDecimal nuNota = inserirPedido.inserePedido(tbhCabVO, BigDecimal.ZERO);

            BigDecimal vlrPedido = cabDAO.findOne("NUNOTA = ?",nuNota).asBigDecimalOrZero("VLRNOTA");
            tbhCabDAO.prepareToUpdate(tbhCabVO).set("VLRSIMULACAO",vlrPedido).update();

            cabDAO.deleteByCriteria("NUNOTA = ?",nuNota);
        }
    }
}

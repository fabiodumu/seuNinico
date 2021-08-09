package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;

public class regraLimpaPedidoRapido implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeDelete(ContextoRegra contextoRegra) throws Exception {
        DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getOldVO();
        boolean tgfCab = "CabecalhoNota".equals(contextoRegra.getPrePersistEntityState().getDao().getEntityName());
        JapeWrapper tbhCabDAO = JapeFactory.dao("AD_TBHCAB");
        if (tgfCab && cabVO.asString("TIPMOV").equals("P")) {
            DynamicVO tbhCabVO = tbhCabDAO.findOne("NUNOTA = ?", cabVO.asBigDecimal("NUNOTA"));
            if (null != tbhCabVO) {
                tbhCabDAO.prepareToUpdate(tbhCabVO)
                        .set("NUNOTA", null)
                        .set("ORDEMCARGA", null)
                        .update();
                tbhCabDAO.prepareToUpdate(tbhCabVO)
                        .set("STATUS", "F")
                        .update();

            }
        }
    }

    @Override
    public void afterInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterUpdate(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterDelete(ContextoRegra contextoRegra) throws Exception {

    }
}

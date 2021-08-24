package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;

public class eventoCompletaDados implements EventoProgramavelJava {
    JapeWrapper parDAO = JapeFactory.dao("Parceiro");//TGFPAR
    JapeWrapper cplDAO = JapeFactory.dao("ComplementoParc");

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();

        DynamicVO parVO = parDAO.findOne("CODPARC = ?",vo.asBigDecimalOrZero("CODPARC"));
        if (parVO!= null) {
            if (vo.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.ZERO) == 0) {
                vo.setProperty("CODTIPOPER", parVO.asBigDecimalOrZero("AD_CODTIPOPER"));
            }
            if (vo.asBigDecimalOrZero("CODTIPVENDA").compareTo(BigDecimal.ZERO) == 0) {
                DynamicVO cplVO = cplDAO.findOne("CODPARC = ?",vo.asBigDecimalOrZero("CODPARC"));
                if (cplVO!=null) {
                    vo.setProperty("CODTIPVENDA", cplVO.asBigDecimalOrZero("SUGTIPNEGSAID"));
                }
            }
            if (vo.asBigDecimalOrZero("CODOBSPADRAO").compareTo(BigDecimal.ZERO) == 0) {
                vo.setProperty("CODOBSPADRAO", BigDecimal.ONE);
            }
            if (vo.asBigDecimalOrZero("CODREG").compareTo(BigDecimal.ZERO) == 0) {
                vo.setProperty("CODREG", parVO.asBigDecimalOrZero("CODREG"));
            }

            if (vo.asBigDecimalOrZero("CODEMP").compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal codemp = BigDecimal.ONE;
                if (parVO.asBigDecimalOrZero("AD_CODEMP").compareTo(BigDecimal.ZERO) != 0) {
                    codemp = parVO.asBigDecimal("AD_CODEMP");
                }
                vo.setProperty("CODEMP", codemp);
            }
            if (null == vo.asString("CIF_FOB")) {
                vo.setProperty("CIF_FOB", "C");
            }
            if (null == vo.asString("STATUS")) {
                vo.setProperty("STATUS", "P");
            }

            String recebeCx = "N";

            if ("S".equals(parVO.asString("AD_RECEBECX"))){
                recebeCx =  "S";
            }
            if (null == vo.asString("RECEBECX")) {
                vo.setProperty("RECEBECX", recebeCx);
            }

            if(vo.asBigDecimalOrZero("PREORDEM").compareTo(BigDecimal.ZERO) == 0) {
                vo.setProperty("PREORDEM", parVO.asBigDecimalOrZero("AD_PREORDEM"));
            }
            if (parVO.asBigDecimalOrZero("AD_SEQCARGA").compareTo(BigDecimal.ZERO)!=0
                    && vo.asBigDecimalOrZero("SEQCARGA").compareTo(BigDecimal.ZERO) == 0) {
                vo.setProperty("SEQCARGA", parVO.asBigDecimalOrZero("AD_SEQCARGA"));
            }

            if (vo.asBigDecimalOrZero("CODPARCORIG").compareTo(BigDecimal.ZERO) == 0) {
                vo.setProperty("CODPARCORIG", BigDecimal.ONE);
            }
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}

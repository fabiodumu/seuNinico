package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.bh.utils.ErroUtils;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;

public class acaoInserirPedidosRapido implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        JapeWrapper cabDAO = JapeFactory.dao("AD_TBHCAB");//Cabeçalho Pedido Rapido
        JapeWrapper parDAO = JapeFactory.dao("Parceiro");//
        JapeWrapper cplDAO = JapeFactory.dao("ComplementoParc");
        Timestamp dtPrev = Timestamp.valueOf(contextoAcao.getParam("DTPREV").toString());
        Calendar cal = Calendar.getInstance();
        cal.setTime(dtPrev);

        String condicao = "1=0";
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                condicao = "AD_DOMINGO = 'S'";
                break;
            case Calendar.MONDAY:
                condicao = "AD_SEGUNDA = 'S'";
                break;
            case Calendar.TUESDAY:
                condicao = "AD_TERCA = 'S'";
                break;
            case Calendar.WEDNESDAY:
                condicao = "AD_QUARTA = 'S'";
                break;
            case Calendar.THURSDAY:
                condicao = "AD_QUINTA = 'S'";
                break;
            case Calendar.FRIDAY:
                condicao = "AD_SEXTA = 'S'";
                break;
            case Calendar.SATURDAY:
                condicao = "AD_SABADO = 'S'";
        }
        Collection<DynamicVO> parceiros = parDAO.find(condicao);
        for(DynamicVO parVO : parceiros) {
            BigDecimal codParc = parVO.asBigDecimal("CODPARC");
            DynamicVO cabVO = cabDAO.findOne("DTPREV = ? AND CODPARC = ?", dtPrev, codParc);
            DynamicVO cplVO = cplDAO.findOne("CODPARC = ?",codParc);
            if (null == cabVO) {
                BigDecimal codEmp = BigDecimal.ONE;
                if (parVO.asBigDecimalOrZero("AD_CODEMP").compareTo(BigDecimal.ZERO)!=0){
                    codEmp = parVO.asBigDecimalOrZero("AD_CODEMP");
                }
                cabDAO.create()
                        .set("DTPREV", dtPrev)
                        .set("CODPARC", codParc)
                        .set("CODTIPOPER", parVO.asBigDecimalOrZero("AD_CODTIPOPER"))
                        .set("PREORDEM", parVO.asBigDecimalOrZero("AD_PREORDEM"))
                        .set("CODEMP", codEmp)
                        .set("CODREG", parVO.asBigDecimalOrZero("CODREG"))
                        .set("CODTIPVENDA", cplVO.asBigDecimalOrZero("SUGTIPNEGSAID"))
                        .set("STATUS", "P")
                        .save();
            }
        }
    }
}

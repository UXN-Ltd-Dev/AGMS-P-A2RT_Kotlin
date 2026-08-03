package kr.co.uxn.agms_p_a2rt

data class ParameterData(
    val times: List<Float>,
    val sensitivities: List<Float>,
    val baseCurrents: List<Float>,
    val slopes: List<Float>,
    val alphas: List<Float>
)

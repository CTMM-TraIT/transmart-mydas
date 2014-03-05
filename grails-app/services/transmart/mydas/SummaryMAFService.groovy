package transmart.mydas

import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.vcf.VcfValues
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasFeatureOrientation
import uk.ac.ebi.mydas.model.DasPhase
import uk.ac.ebi.mydas.model.DasType

import javax.annotation.PostConstruct

/**
 * Created by j.hudecek on 5-3-14.
 */
class SummaryMAFService extends  VcfServiceAbstract {

    @PostConstruct
    void init() {
        super.init()
        resource = highDimensionResourceService.getSubResourceForType 'cohortMAF'
        //TODO Choose correct cvId(3-d parameter) from http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=SO
        projectionName = 'cohortMAF_values'
        dasTypes = [projectionName];
    }

    @Override
    protected void getSpecificFeatures(RegionRow region, Object assays, Collection<DasType> dasTypes, Map<String, List<DasFeature>> featuresPerSegment) {
        constructSegmentFeaturesMap([region], getSummaryMafFeature, featuresPerSegment)
    }

    private def getSummaryMafFeature = { VcfValues val ->
        if (!val.maf || val.maf <= 0) {
            return []
        }

        def linkMap = val.rsId == '.' ? [:]
                : [(new URL("http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=${val.rsId}")): 'NCBI SNP Ref']

        [new DasFeature(
                // feature id - any unique id that represent this feature
                "smaf-${val.rsId}",
                // feature label
                'Minor Allele Frequency',
                // das type
                new DasType('smaf', "", "", ""),
                // das method TODO: pls find out what is actually means
                dasMethod,
                // start pos
                val.position.toInteger(),
                // end pos
                val.position.toInteger(),
                // value - this is where Minor Allele Freq (MAF) value is placed
                (val.additionalInfo['AF'] ?: '0') as double,
                DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
                DasPhase.PHASE_NOT_APPLICABLE,
                //notes
                ["RefSNP=${val.rsId}",
                        "REF=${val.referenceAllele}",
                        "ALT=${val.alternativeAlleles.join(',')}",
                        "AlleleCount=${val.additionalInfo['AC'] ?: NA}",
                        "AlleleFrequency=${val.additionalInfo['AF'] ?: NA}",
                        "TotalAllele=${val.additionalInfo['AN'] ?: NA}",
                        "BaseQRankSum=${val.additionalInfo['BaseQRankSum'] ?: NA}",
                        "MQRankSum=${val.additionalInfo['MQRankSum'] ?: NA}",
                        "dbSNPMembership=${val.additionalInfo['DB'] ?: 'No'}",
                        "VariantClassification=${val.additionalInfo['VC'] ?: NA}",
                        "QualityOfDepth=${val.qualityOfDepth ?: NA}",
                        "GenomicVariantTypes=${val.genomicVariantTypes.join(',')}"]*.toString(),
                //links
                linkMap,
                //targets
                [],
                //parents
                [],
                //parts
                []
        )]
    }
}
